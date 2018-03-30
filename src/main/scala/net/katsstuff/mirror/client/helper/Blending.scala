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
package net.katsstuff.mirror.client.helper

import net.minecraft.client.renderer.GlStateManager.{DestFactor, SourceFactor}
import net.minecraft.client.renderer.{GlStateManager, OpenGlHelper}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
case class Blending(source: GlStateManager.SourceFactor, dest: GlStateManager.DestFactor) {

  /**
    * Applies this blending mode.
    */
  def apply(): Unit = GlStateManager.blendFunc(source, dest)
}

/**
  * A helper for the most commonly used blending modes.
  */
@SideOnly(Side.CLIENT)
object Blending {
  val Normal        = Blending(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
  val Alpha         = Blending(SourceFactor.ONE, DestFactor.SRC_ALPHA)
  val PreAlpha      = Blending(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA)
  val Multiply      = Blending(SourceFactor.DST_COLOR, DestFactor.ONE_MINUS_SRC_ALPHA)
  val Additive      = Blending(SourceFactor.ONE, DestFactor.ONE)
  val AdditiveDark  = Blending(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_COLOR)
  val OverlayDark   = Blending(SourceFactor.SRC_COLOR, DestFactor.ONE)
  val AdditiveAlpha = Blending(SourceFactor.SRC_ALPHA, DestFactor.ONE)
  val InvertedAdd   = Blending(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ONE_MINUS_SRC_COLOR)

  def lightMap(u: Float, v: Float): Unit = OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, u, v)
}
