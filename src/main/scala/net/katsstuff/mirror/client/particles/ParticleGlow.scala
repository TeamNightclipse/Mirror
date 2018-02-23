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

import java.util.concurrent.ThreadLocalRandom

import net.katsstuff.mirror.data.Vector3
import net.minecraft.client.Minecraft
import net.minecraft.world.World

class ParticleGlow(
    _world: World,
    pos: Vector3,
    motion: Vector3,
    r: Float,
    g: Float,
    b: Float,
    scale: Float,
    lifetime: Int,
    val tpe: GlowTexture
) extends AbstractMirrorParticle(_world, pos, Vector3.Zero)
    with IMirrorParticle {

  private val initScale = scale

  {
    val colorR = if (r > 1.0) r / 255.0f else r
    val colorG = if (g > 1.0) g / 255.0f else g
    val colorB = if (b > 1.0) b / 255.0f else b
    setRBGColorF(colorR, colorG, colorB)
  }
  particleMaxAge = lifetime
  particleScale = scale
  motionX = motion.x
  motionY = motion.y
  motionZ = motion.z
  particleAngle = 2.0f * Math.PI.toFloat

  setParticleTexture(Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(tpe.getTexture.toString))

  override def getBrightnessForRender(pTicks: Float) = 255

  override def shouldDisableDepth = true

  override def getFXLayer = 1

  override def onUpdateGlow(): Unit = {
    super.onUpdateGlow()
    if (ThreadLocalRandom.current.nextInt(6) == 0) particleAge += 1
    val lifeCoeff = particleAge.toFloat / particleMaxAge.toFloat
    this.particleScale = initScale - initScale * lifeCoeff
    this.particleAlpha = 1.0f - lifeCoeff
    this.prevParticleAngle = particleAngle
    particleAngle += 1.0f
  }

  override def alive: Boolean = particleAge < particleMaxAge

  override def isAdditive = true

  override def ignoreDepth = false
}
