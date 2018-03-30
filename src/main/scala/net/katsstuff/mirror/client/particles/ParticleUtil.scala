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
package net.katsstuff.mirror.client.particles

import java.util.Random

import net.katsstuff.mirror.Mirror
import net.katsstuff.mirror.client.ClientProxy
import net.katsstuff.mirror.data.Vector3
import net.katsstuff.mirror.network.scalachannel.TargetPoint
import net.katsstuff.mirror.network.{MirrorPacketHandler, ParticlePacket}
import net.minecraft.client.Minecraft
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object ParticleUtil {
  private val random  = new Random
  private var counter = 0

  /**
    * Spawns a glow particle on the client, accounting for client settings.
    * @param world The world to render the particle in.
    * @param pos The position to render the particle at.
    * @param motion The motion to give to the particle.
    * @param r The red color of the particle.
    * @param g The green color of the particle.
    * @param b The blue color of the particle.
    * @param scale The scale of the particle.
    * @param lifetime The lifetime of the particle.
    * @param texture The texture of the particle.
    */
  @SideOnly(Side.CLIENT)
  def spawnParticleGlow(
      world: World,
      pos: Vector3,
      motion: Vector3,
      r: Float,
      g: Float,
      b: Float,
      scale: Float,
      lifetime: Int,
      texture: GlowTexture
  ): Unit = {
    counter += random.nextInt(3)
    val particleSetting = Minecraft.getMinecraft.gameSettings.particleSetting
    val particleComp    = if (particleSetting == 0) 1 else 2 * particleSetting
    if (counter % particleComp == 0) {
      Mirror.proxy
        .asInstanceOf[ClientProxy]
        .particleRenderer
        .addParticle(new ParticleGlow(world, pos, motion, r, g, b, scale, lifetime, texture))
    }
  }

  /**
    * Sends a packet to render a glow particle centered around a location.
    * @param world The world to render the particle in.
    * @param pos The position to render the particle at.
    * @param motion The motion to give to the particle.
    * @param r The red color of the particle.
    * @param g The green color of the particle.
    * @param b The blue color of the particle.
    * @param scale The scale of the particle.
    * @param lifetime The lifetime of the particle.
    * @param texture The texture of the particle.
    * @param range The range to send the packet in.
    */
  def spawnParticleGlowPacket(
      world: World,
      pos: Vector3,
      motion: Vector3,
      r: Float,
      g: Float,
      b: Float,
      scale: Float,
      lifetime: Int,
      texture: GlowTexture,
      range: Int
  ): Unit =
    MirrorPacketHandler
      .sendToAllAround(
        new ParticlePacket(pos, motion, r, g, b, scale, lifetime, texture),
        TargetPoint.around(world.provider.getDimension, pos, range)
      )
}
