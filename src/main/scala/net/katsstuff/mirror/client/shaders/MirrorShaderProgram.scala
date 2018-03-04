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

case class MirrorShaderProgram(shaders: Seq[MirrorShader], programId: Int, uniformMap: Map[String, MirrorUniform]) {

  def delete(): Unit = {
    shaders.foreach(_.delete())
    OpenGlHelper.glDeleteProgram(programId)
  }

  def begin(): Unit =
    OpenGlHelper.glUseProgram(programId)

  def getUniformS(name: String): Option[MirrorUniform] = uniformMap.get(name)

  def getUniformJ(name: String): Optional[MirrorUniform] = uniformMap.get(name).toOptional

  def uploadUniforms(): Unit = uniformMap.values.foreach(_.upload())

  def end(): Unit = OpenGlHelper.glUseProgram(0)

  def uniforms: UniformSyntax = new UniformSyntax(this)
}
object MirrorShaderProgram {

  private[mirror] def missingShaderProgram(shaders: Seq[MirrorShader], uniforms: Seq[UniformBase]) =
    MirrorShaderProgram(shaders, 0, uniforms.map(base => base.name -> new NOOPUniform(base.tpe, base.count)).toMap)

  @throws[ShaderException]
  def create(
      shaders: util.Map[ResourceLocation, MirrorShader],
      uniforms: util.List[UniformBase],
      strictUniforms: Boolean
  ): MirrorShaderProgram = create(shaders.asScala.toMap, uniforms.asScala, strictUniforms)

  @throws[ShaderException]
  def create(
      shaders: Map[ResourceLocation, MirrorShader],
      uniforms: Seq[UniformBase],
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
      case UniformBase(name, tpe, count) =>
        val location = OpenGlHelper.glGetUniformLocation(programId, name)
        if (location == -1) {
          if (strictUniforms) {
            throw new ShaderException("Error when getting uniform location")
          } else new NOOPUniform(tpe, count)
        }
        name -> MirrorUniform.create(location, tpe, count)
    }.toMap

    MirrorShaderProgram(shaders.values.toSeq, programId, uniformMap)
  }

}
