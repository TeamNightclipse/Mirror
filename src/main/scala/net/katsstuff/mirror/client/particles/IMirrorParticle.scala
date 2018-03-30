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

/**
  * Represents a Mirror particle used updated on the client thread. Can inherit
  * from the minecraft particle class, but doesn't have to.
  */
trait IMirrorParticle {

  /**
    * Called when updating this particle.
    */
  def onUpdateGlow(): Unit

  /**
    * Called when rendering this particle.
    */
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

  /**
    * Checks if this particle should render.
    */
  def shouldRender: Boolean = true

  /**
    * Checks if this particle is additive.
    */
  def isAdditive: Boolean

  /**
    * Checks if this particle ignores depth.
    */
  def ignoreDepth: Boolean

  /**
    * Checks if this particle is still alive.
    */
  def alive: Boolean
}
