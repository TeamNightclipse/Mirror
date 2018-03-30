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
package net.katsstuff.mirror.network

import net.katsstuff.mirror.client.particles.{GlowTexture, ParticleUtil}
import net.katsstuff.mirror.data.Vector3
import net.katsstuff.mirror.network.scalachannel.{ClientMessageHandler, MessageConverter}
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetHandlerPlayClient

/**
  * A packet which signals that a client should render a particle at some
  * location with some properties.
  */
case class ParticlePacket(
    pos: Vector3,
    motion: Vector3,
    r: Float,
    g: Float,
    b: Float,
    scale: Float,
    lifetime: Int,
    texture: GlowTexture
)
object ParticlePacket {

  implicit val converter: MessageConverter[ParticlePacket] = MessageConverter.mkDeriver[ParticlePacket].apply

  //noinspection ConvertExpressionToSAM
  implicit val handler: ClientMessageHandler[ParticlePacket, Unit] = new ClientMessageHandler[ParticlePacket, Unit] {
    override def handle(netHandler: NetHandlerPlayClient, a: ParticlePacket): Option[Unit] = {
      scheduler.addScheduledTask(ParticlePacketRunnable(netHandler, a))
      None
    }
  }
}

case class ParticlePacketRunnable(server: NetHandlerPlayClient, packet: ParticlePacket) extends Runnable {
  override def run(): Unit =
    ParticleUtil.spawnParticleGlow(
      Minecraft.getMinecraft.player.world,
      packet.pos,
      packet.motion,
      packet.r,
      packet.g,
      packet.b,
      packet.scale,
      packet.lifetime,
      packet.texture
    )
}
