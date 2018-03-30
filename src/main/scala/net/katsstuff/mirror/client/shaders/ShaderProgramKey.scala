package net.katsstuff.mirror.client.shaders

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
