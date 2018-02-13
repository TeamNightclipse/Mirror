/*
 * This file is part of Mirror, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 TeamNightclipse
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

import scala.collection.mutable
import scala.util.control.NonFatal

import net.katsstuff.mirror.client.helper.MirrorRenderHelper
import net.katsstuff.mirror.helper.MirrorLogHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.{IResourceManager, IResourceManagerReloadListener}
import net.minecraft.crash.CrashReport
import net.minecraft.util.{ReportedException, ResourceLocation}
import net.minecraftforge.fml.common.ProgressManager
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class ShaderManager(resourceManager: IResourceManager) extends IResourceManagerReloadListener {
  private val shaderPrograms = mutable.Map.empty[ResourceLocation, MirrorShaderProgram]
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
      MirrorShader.compileShader(location, shaderType, resourceManager)
    } catch {
      case e: IOException =>
        MirrorLogHelper.warn(s"Failed to load shader: $location", e)
        MirrorShader.missingShader(shaderType)
      case e: ShaderException =>
        MirrorLogHelper.warn(s"Failed to compile shader: $location", e)
        MirrorShader.missingShader(shaderType)
    }
  }

  def createShaderProgram(
      location: ResourceLocation,
      shaderTypes: Seq[ShaderType],
      uniforms: Seq[UniformBase],
      strictUniforms: Boolean = false
  ): MirrorShaderProgram = {
    val shaders = shaderTypes.map(_ -> location).toMap
    createShaderProgram(shaders, uniforms, strictUniforms)
  }

  def createShaderProgram(
      shaders: Map[ShaderType, ResourceLocation],
      uniforms: Seq[UniformBase],
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

  def initShader(
      shaderLocation: ResourceLocation,
      shaderTypes: Seq[ShaderType],
      uniforms: Seq[UniformBase],
      init: MirrorShaderProgram => Unit
  ): Unit = {
    shaderProgramsInits.addBinding(shaderLocation, init)
    val shader = shaderPrograms.getOrElseUpdate(shaderLocation, createShaderProgram(shaderLocation, shaderTypes, uniforms))
    init(shader)
  }

  def getShaderProgram(shaderLocation: ResourceLocation): Option[MirrorShaderProgram] =
    shaderPrograms.get(shaderLocation)

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
    val res = for ((resource, program) <- shaderPrograms) yield {
      programBar.step(resource.toString)
      program.delete()
      val newProgram = createShaderProgram(resource, program.shaders.map(_.shaderType), program.uniformMap.map {
        case (name, uniform) => UniformBase(name, uniform.tpe, uniform.count)
      }.toSeq)
      shaderProgramsInits.get(resource).foreach(_.foreach(init => init(newProgram)))

      resource -> newProgram
    }

    shaderPrograms ++= res
    ProgressManager.pop(reloadBar)
  }
}

@SideOnly(Side.CLIENT)
object ShaderManager extends ShaderManager(Minecraft.getMinecraft.getResourceManager)
