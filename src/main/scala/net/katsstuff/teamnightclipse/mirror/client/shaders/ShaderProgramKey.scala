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
package net.katsstuff.teamnightclipse.mirror.client.shaders

import net.minecraft.util.ResourceLocation
import shapeless._
import shapeless.tag._
import shapeless.labelled._

/**
  * A key used for typeful shader programs coming from Scala.
  * @param location The [[ResourceLocation]] associated with this shader.
  * @param uniforms The uniform HList used by this shader.
  */
case class ShaderProgramKey[+Uniforms <: HList](location: ResourceLocation, uniforms: Option[Uniforms])
object ShaderProgramKey {
  object mapUniforms extends Poly1 {
    //noinspection TypeAnnotation
    implicit def atUniformBase[Name <: String, Type <: UniformType] =
      at[FieldType[Name, UniformBase[Type]]](b => field[Symbol @@ Name](b.tpe))
  }

  def apply[Uniforms <: HList](location: ResourceLocation, uniforms: Uniforms)(
      implicit mapCreateKey: ops.hlist.Mapper[mapUniforms.type, Uniforms]
  ): ShaderProgramKey[mapCreateKey.Out] = new ShaderProgramKey(location, Some(mapCreateKey(uniforms)))
}
