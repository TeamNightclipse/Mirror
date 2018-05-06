/*
 * This file is part of Mirror, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018 TeamNightclipse
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.katsstuff.mirror.network.scalachannel

import java.lang.ref.WeakReference
import java.util

import scala.annotation.tailrec
import scala.collection.mutable
import scala.reflect.ClassTag

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelPromise}
import io.netty.handler.codec.{MessageToMessageDecoder, MessageToMessageEncoder}
import io.netty.util.AttributeKey
import net.katsstuff.mirror.Mirror
import net.katsstuff.mirror.helper.MirrorLogHelper
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket

object ScalaIndexedMessageChannel {
  val InboundPacketTracker: AttributeKey[ThreadLocal[WeakReference[FMLProxyPacket]]] =
    AttributeKey.valueOf(s"${Mirror.Id}:inboundpacket")
}

@Sharable
class ScalaIndexedMessageChannel extends ChannelDuplexHandler {

  private val discriminatorToConverter = mutable.HashMap.empty[Byte, MessageConverter[_]]
  private val classToDiscriminator     = mutable.HashMap.empty[Class[_], Byte]

  private val encoder = new MessageToMessageEncoder[AnyRef]() {

    def getDiscriminator(msgClazz: Class[_]): Option[Byte] = {
      if(msgClazz != null) {
        val directParents = classToDiscriminator.get(msgClazz).orElse(getDiscriminator(msgClazz.getSuperclass))
        directParents match {
          case res @ Some(_) => res
          case None =>
            @tailrec
            def checkInterfaces(interfaces: Array[Class[_]], idx: Int): Option[Byte] = {
              if(interfaces.isDefinedAt(idx)) {
                getDiscriminator(interfaces(idx)) match {
                  case res @ Some(_) => res
                  case None => checkInterfaces(interfaces, idx + 1)
                }
              } else None
            }

            checkInterfaces(msgClazz.getInterfaces, 0)
        }

      } else None
    }

    override def acceptOutboundMessage(msg: AnyRef): Boolean = getDiscriminator(msg.getClass).nonEmpty

    override def encode(ctx: ChannelHandlerContext, msg: AnyRef, out: util.List[AnyRef]): Unit = {
      val buf = new PacketBuffer(Unpooled.buffer)

      val packetData = for {
        discriminator <- getDiscriminator(msg.getClass)
        converter     <- discriminatorToConverter.get(discriminator)
      } yield (discriminator, converter)

      packetData match {
        case Some((discriminator, converter)) =>
          buf.writeByte(discriminator)
          converter.asInstanceOf[MessageConverter[Any]].writeBytes(msg, buf)
          val proxy = new FMLProxyPacket(buf, ctx.channel.attr(NetworkRegistry.FML_CHANNEL).get)
          val ref   = ctx.attr(ScalaIndexedMessageChannel.InboundPacketTracker).get.get
          val old   = if (ref == null) null else ref.get
          if (old != null) proxy.setDispatcher(old.getDispatcher)
          out.add(proxy)
        case None => throw new IllegalStateException(s"No discriminator or converter found for $msg")
      }
    }
  }

  private val decoder = new MessageToMessageDecoder[AnyRef]() {
    override def acceptInboundMessage(msg: AnyRef): Boolean = msg.isInstanceOf[FMLProxyPacket]
    override def decode(ctx: ChannelHandlerContext, msg: AnyRef, out: util.List[AnyRef]): Unit = {
      val fmlProxyPacket = msg.asInstanceOf[FMLProxyPacket]

      val payload = fmlProxyPacket.payload.duplicate
      if (payload.readableBytes < 1)
        MirrorLogHelper.error(
          s"The ScalaIndexedMessageChannel has received an empty buffer on channel ${ctx.channel
            .attr(NetworkRegistry.FML_CHANNEL)}, likely a result of a LAN server issue. Pipeline parts : ${ctx.pipeline.toString}"
        )
      val discriminator = payload.readByte
      discriminatorToConverter.get(discriminator) match {
        case Some(converter) =>
          ctx
            .attr(ScalaIndexedMessageChannel.InboundPacketTracker)
            .get
            .set(new WeakReference[FMLProxyPacket](fmlProxyPacket))
          val newMsg = converter.readBytes(payload.slice())
          out.add(newMsg.asInstanceOf[AnyRef])
        case None =>
          throw new IllegalStateException(
            s"Undefined message for discriminator $discriminator in channel ${fmlProxyPacket.channel}"
          )
      }
    }
  }

  def addDiscriminator[A](discriminator: Byte, converter: MessageConverter[A])(implicit classTag: ClassTag[A]): Unit = {
    discriminatorToConverter.put(discriminator, converter)
    classToDiscriminator.put(classTag.runtimeClass, discriminator)
  }

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = {
    ctx.attr(ScalaIndexedMessageChannel.InboundPacketTracker).set(new ThreadLocal[WeakReference[FMLProxyPacket]])
    super.handlerAdded(ctx)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit =
    decoder.channelRead(ctx, msg)

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit =
    encoder.write(ctx, msg, promise)
}
