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

import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.entity.Entity

trait IGlowParticle {
  //These methods need to delegate to the respective minecraft methods
  def onUpdateGlow(): Unit
  def renderParticleGlow(
      buffer: BufferBuilder,
      entityIn: Entity,
      partialTicks: Float,
      rotationX: Float,
      rotationZ: Float,
      rotationYZ: Float,
      rotationXY: Float,
      rotationXZ: Float
  ): Unit

  def shouldRender: Boolean = true

  def isAdditive:  Boolean
  def ignoreDepth: Boolean
  def alive:       Boolean
}
