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

import java.io.IOException
import java.util
import java.util.function.Consumer

import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.util.control.NonFatal

import net.katsstuff.mirror.client.helper.MirrorRenderHelper
import net.katsstuff.mirror.client.shaders.ShaderProgramKey.mapUniforms
import net.katsstuff.mirror.helper.MirrorLogHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.{IResourceManager, IResourceManagerReloadListener}
import net.minecraft.crash.CrashReport
import net.minecraft.util.{ReportedException, ResourceLocation}
import net.minecraftforge.fml.common.ProgressManager
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import shapeless._
import shapeless.ops.record.MapValues

@SideOnly(Side.CLIENT)
class ShaderManagerBase(resourceManager: IResourceManager) extends IResourceManagerReloadListener {
  private val shaderPrograms = mutable.Map.empty[ShaderProgramKey[_ <: HList], MirrorShaderProgram]
  private val shaderProgramsInits = {
    type ShaderInit = MirrorShaderProgram => Unit
    new mutable.HashMap[ResourceLocation, mutable.Set[ShaderInit]] with mutable.MultiMap[ResourceLocation, ShaderInit]
  }
  private val shaderObjects = mutable.Map.empty[ResourceLocation, MirrorShader]
  MirrorRenderHelper.registerResourceReloadListener(this)

  def getShader(location: ResourceLocation, shaderType: ShaderType): MirrorShader =
    shaderObjects.getOrElseUpdate(location, compileShader(location, shaderType))

  def compileShader(location: ResourceLocation, shaderType: ShaderType): MirrorShader = {
    try {
      MirrorShader.compile(location, shaderType, resourceManager)
    } catch {
      case e: IOException =>
        MirrorLogHelper.warn(s"Failed to load shader: $location", e)
        MirrorShader.missingShader(shaderType)
      case e: ShaderException =>
        MirrorLogHelper.warn(s"Failed to compile shader: $location", e)
        MirrorShader.missingShader(shaderType)
    }
  }

  def createProgram(
      location: ResourceLocation,
      shaderTypes: util.List[ShaderType],
      uniforms: util.Map[String, UniformBase[_ <: UniformType]],
      strictUniforms: Boolean
  ): MirrorShaderProgram = createProgram(location, shaderTypes.asScala, uniforms.asScala.toMap, strictUniforms)

  def createProgram(
      location: ResourceLocation,
      shaderTypes: Seq[ShaderType],
      uniforms: Map[String, UniformBase[_ <: UniformType]],
      strictUniforms: Boolean = true
  ): MirrorShaderProgram = {
    val shaders = shaderTypes.map(_ -> location).toMap
    createComplexProgram(shaders, uniforms, strictUniforms)
  }

  def createComplexProgram(
      shaders: util.Map[ShaderType, ResourceLocation],
      uniforms: util.Map[String, UniformBase[_ <: UniformType]],
      strictUniforms: Boolean
  ): MirrorShaderProgram = createComplexProgram(shaders.asScala.toMap, uniforms.asScala.toMap, strictUniforms)

  def createComplexProgram(
      shaders: Map[ShaderType, ResourceLocation],
      uniforms: Map[String, UniformBase[_ <: UniformType]],
      strictUniforms: Boolean
  ): MirrorShaderProgram = {
    val newShaders = shaders.map {
      case (tpe, location) =>
        val shaderLocation = new ResourceLocation(s"$location.${tpe.extension}")
        val shader         = getShader(shaderLocation, tpe)
        shaderLocation -> shader
    }

    try {
      MirrorShaderProgram.create(newShaders, uniforms, strictUniforms)
    } catch {
      case e: ShaderException =>
        MirrorLogHelper.warn(s"Failed to create shader: $shaders", e)
        MirrorShaderProgram.missingShaderProgram(newShaders.values.toSeq, uniforms)
      case NonFatal(throwable) =>
        val crashReport         = CrashReport.makeCrashReport(throwable, "Registering shaders")
        val crashReportCategory = crashReport.makeCategory("Resource locations being registered")
        crashReportCategory.addCrashSection("Resource locations", shaders.values)
        throw new ReportedException(crashReport)
    }
  }

  private def getOrElseAddProgram(
      key: ShaderProgramKey[_ <: HList],
      shaderTypes: Seq[ShaderType],
      uniforms: Map[String, UniformBase[_ <: UniformType]],
      strictUniforms: Boolean
  ) = {
    shaderPrograms.find(_._1.location == key.location) match {
      case Some((programKey, program)) =>
        if (key.uniforms.nonEmpty) {
          require(key == programKey, s"Tried to add a new shader with different uniforms as ${key.location}")
        }

        program
      case None =>
        val program = createProgram(key.location, shaderTypes, uniforms, strictUniforms)
        shaderPrograms.put(key, program)
        program
    }
  }

  def initProgram(
      shaderLocation: ResourceLocation,
      shaderTypes: util.List[ShaderType],
      uniforms: util.Map[String, UniformBase[_ <: UniformType]],
      init: Consumer[MirrorShaderProgram]
  ): Unit =
    initProgram(shaderLocation, shaderTypes.asScala, uniforms.asScala.toMap, program => init.accept(program))

  def initProgram(
      shaderLocation: ResourceLocation,
      shaderTypes: Seq[ShaderType],
      uniforms: Map[String, UniformBase[_ <: UniformType]],
      init: MirrorShaderProgram => Unit
  ): Unit = {
    shaderProgramsInits.addBinding(shaderLocation, init)
    init(getOrElseAddProgram(ShaderProgramKey(shaderLocation, None), shaderTypes, uniforms, strictUniforms = true))
  }

  def initTypedProgram[Uniforms <: HList, Keys <: String, Values <: UniformBase[_ <: UniformType]](
      shaderLocation: ResourceLocation,
      shaderTypes: Seq[ShaderType],
      uniforms: Uniforms,
      init: MirrorShaderProgram => Unit
  )(
      implicit mapCreateKey: ops.hlist.Mapper[mapUniforms.type, Uniforms],
      toMap: ops.record.ToMap.Aux[Uniforms, Keys, Values]
  ): ShaderProgramKey[mapCreateKey.Out] = {
    shaderProgramsInits.addBinding(shaderLocation, init)
    val key = ShaderProgramKey(shaderLocation, uniforms)
    val uniformMap = toMap(uniforms)
    val lubedUniforms = uniformMap.map { case (k, v) =>
      (k: String) -> (v: UniformBase[_ <: UniformType])
    }
    init(getOrElseAddProgram(key, shaderTypes, lubedUniforms, strictUniforms = true))
    key
  }

  def getProgram(shaderLocation: ResourceLocation): Option[MirrorShaderProgram] =
    shaderPrograms.find(_._1.location == shaderLocation).map(_._2)

  def getProgram[Uniforms <: HList](
      key: ShaderProgramKey[Uniforms]
  ): Option[MirrorShaderProgram.TypeLevelProgram[Uniforms]] = {
    require(key.uniforms.isDefined, "This method should only be used for keys which have Uniforms bound to them")
    shaderPrograms.get(key).map(program => MirrorShaderProgram.TypeLevelProgram(program, key.uniforms.get))
  }

  override def onResourceManagerReload(resourceManager: IResourceManager): Unit = {
    val reloadBar = ProgressManager.push("Reloading Shader Manager", 0)

    val shaderBar = ProgressManager.push("Reloading shaders", shaderObjects.size)
    val newShaders = for ((resource, shader) <- shaderObjects) yield {
      shaderBar.step(resource.toString)
      shader.delete()
      resource -> compileShader(resource, shader.shaderType)
    }

    shaderObjects.clear()
    shaderObjects ++= newShaders
    ProgressManager.pop(shaderBar)

    val programBar = ProgressManager.push("Reloading shader programs", shaderPrograms.size)
    val res = for ((key @ ShaderProgramKey(resource, _), program) <- shaderPrograms) yield {
      programBar.step(resource.toString)
      program.delete()
      val newProgram = createProgram(resource, program.shaders.map(_.shaderType), program.uniformMap.map {
        case (name, uniform) => name -> UniformBase(uniform.tpe, uniform.count)
      })
      shaderProgramsInits.get(resource).foreach(_.foreach(init => init(newProgram)))

      key -> newProgram
    }

    shaderPrograms ++= res
    ProgressManager.pop(reloadBar)
  }
}

@SideOnly(Side.CLIENT)
object ShaderManager extends ShaderManagerBase(Minecraft.getMinecraft.getResourceManager)
