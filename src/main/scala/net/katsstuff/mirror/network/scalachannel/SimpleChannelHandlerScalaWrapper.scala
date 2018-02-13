/*
 * This file is part of Mirror, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 TeamNightclipse
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

import scala.reflect.ClassTag

import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import net.katsstuff.mirror.helper.MirrorLogHelper
import net.minecraftforge.fml.common.network.{FMLOutboundHandler, NetworkRegistry}
import net.minecraftforge.fml.relauncher.Side

class SimpleChannelHandlerScalaWrapper[A, Reply](handler: MessageHandler[A, Reply], side: Side)(implicit classTag: ClassTag[A])
    extends SimpleChannelInboundHandler[A](classTag.runtimeClass.asInstanceOf[Class[_ <: A]]) {

  @throws[Exception]
  override protected def channelRead0(ctx: ChannelHandlerContext, msg: A): Unit = {
    val iNetHandler = ctx.channel.attr(NetworkRegistry.NET_HANDLER).get
    val reply       = handler.handle(iNetHandler, msg)

    reply.foreach { rep =>
      ctx.channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.REPLY)
      ctx.writeAndFlush(rep).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
    }
  }

  @throws[Exception]
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    MirrorLogHelper.error("SimpleChannelHandlerScalaWrapper exception", cause)
    super.exceptionCaught(ctx, cause)
  }
}
