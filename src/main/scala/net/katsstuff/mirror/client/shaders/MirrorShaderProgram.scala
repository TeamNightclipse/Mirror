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
package net.katsstuff.mirror.client.shaders

import java.util
import java.util.Optional

import scala.collection.JavaConverters._

import net.katsstuff.mirror.scalastuff.MirrorImplicits._
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.util.ResourceLocation
import shapeless._

/**
  * Represents a Mirror shader program.
  * @param shaders The shaders used by this program.
  * @param programId The program id.
  * @param uniformMap A map of the uniforms of this program.
  */
case class MirrorShaderProgram(
    shaders: Seq[MirrorShader],
    programId: Int,
    uniformMap: Map[String, MirrorUniform[_ <: UniformType]]
) {

  /**
    * Deletes this program, and all the shaders it uses. Be careful with this one.
    */
  def delete(): Unit = {
    shaders.foreach(_.delete())
    OpenGlHelper.glDeleteProgram(programId)
  }

  /**
    * Begins to use this program.
    */
  def begin(): Unit =
    OpenGlHelper.glUseProgram(programId)

  /**
    * Gets a uniform. Scala-API.
    * @param name The name of the uniform.
    */
  def getUniformS(name: String): Option[MirrorUniform[_ <: UniformType]] = uniformMap.get(name)

  /**
    * Gets a uniform. Java-API.
    * @param name The name of the uniform.
    */
  def getUniformJ(name: String): Optional[MirrorUniform[_ <: UniformType]] = uniformMap.get(name).toOptional

  /**
    * Uploads any dirty uniforms.
    */
  def uploadUniforms(): Unit = uniformMap.values.foreach(_.upload())

  /**
    * Ends the use of this program.
    */
  def end(): Unit = OpenGlHelper.glUseProgram(0)
}
object MirrorShaderProgram {

  private[mirror] def missingShaderProgram(
      shaders: Seq[MirrorShader],
      uniforms: Map[String, UniformBase[_ <: UniformType]]
  ) =
    MirrorShaderProgram(shaders, 0, uniforms.map { case (name, base) => name -> new NOOPUniform(base.tpe, base.count) })

  /**
    * Creates a new shader program with no management. Java-API.
    * @param shaders A map of where to find shaders, and the shaders themselves.
    * @param uniforms A map of uniform names and a base which the uniforms will be built on.
    * @param strictUniforms If the uniforms should be strict (missing uniform == exception).
    */
  @throws[ShaderException]
  def create(
      shaders: util.Map[ResourceLocation, MirrorShader],
      uniforms: util.Map[String, UniformBase[_ <: UniformType]],
      strictUniforms: Boolean
  ): MirrorShaderProgram = create(shaders.asScala.toMap, uniforms.asScala.toMap, strictUniforms)

  /**
    * Creates a new shader program with no management. Scala-API.
    * @param shaders A map of where to find shaders, and the shaders themselves.
    * @param uniforms A map of uniform names and a base which the uniforms will be built on.
    * @param strictUniforms If the uniforms should be strict (missing uniform == exception).
    */
  @throws[ShaderException]
  def create(
      shaders: Map[ResourceLocation, MirrorShader],
      uniforms: Map[String, UniformBase[_ <: UniformType]],
      strictUniforms: Boolean = true
  ): MirrorShaderProgram = {
    val programId = OpenGlHelper.glCreateProgram()

    shaders.values.foreach { shader =>
      OpenGlHelper.glAttachShader(programId, shader.id)
    }
    OpenGlHelper.glLinkProgram(programId)
    val errorId = OpenGlHelper.glGetProgrami(programId, OpenGlHelper.GL_LINK_STATUS)

    if (errorId == 0) {
      throw new ShaderException(s"""|Error encountered when linking program containing shaders: $shaders. Log output:
                                    |${OpenGlHelper.glGetProgramInfoLog(programId, 32768)}""".stripMargin)
    }

    val uniformMap = uniforms.map {
      case (name, UniformBase(tpe, count)) =>
        val location = OpenGlHelper.glGetUniformLocation(programId, name)
        if (location == -1) {
          if (strictUniforms) {
            throw new ShaderException("Error when getting uniform location")
          } else new NOOPUniform(tpe, count)
        }
        name -> MirrorUniform.create(location, tpe, count)
    }

    MirrorShaderProgram(shaders.values.toSeq, programId, uniformMap)
  }

  /**
    * Creates a shader with more type information around uniforms. This will allow using the dynamic uniform syntax.
    * @param shaders A map of where to find shaders, and the shaders themselves.
    * @param uniforms A HList of all the uniforms.
    * @param strictUniforms If the uniforms should be strict (missing uniform == exception).
    */
  @throws[ShaderException]
  def createTyped[Uniforms <: HList](
      shaders: Map[ResourceLocation, MirrorShader],
      uniforms: Uniforms,
      strictUniforms: Boolean = true
  )(
      implicit toMap: ops.record.ToMap.Aux[Uniforms, String, UniformBase[_ <: UniformType]],
      mapCreateKey: ops.hlist.Mapper[ShaderProgramKey.mapUniforms.type, Uniforms]
  ): (mapCreateKey.Out, MirrorShaderProgram) = {
    val keyRecord = mapCreateKey(uniforms)
    val program   = create(shaders, toMap(uniforms), strictUniforms)
    (keyRecord, program)
  }

  case class TypeLevelProgram[Uniforms <: HList](program: MirrorShaderProgram, uniformsList: Uniforms) {

    def uniforms: UniformSyntax[Uniforms] = new UniformSyntax(this)
  }

}
