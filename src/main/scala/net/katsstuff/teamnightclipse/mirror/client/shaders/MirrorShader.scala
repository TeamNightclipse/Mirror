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

import java.io.IOException

import scala.collection.JavaConverters._

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.lwjgl.BufferUtils

import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.resources.{IResource, IResourceManager}
import net.minecraft.util.ResourceLocation

/**
  * Represents an individual shader within Mirror.
  * @param id The id of the shader.
  * @param shaderType The type of the shader.
  */
case class MirrorShader(id: Int, shaderType: ShaderType) {
  private var deleted: Boolean = false

  /**
    * Deletes this shader. Be very careful when using this.
    */
  def delete(): Unit =
    if (!deleted) {
      OpenGlHelper.glDeleteShader(id)
      deleted = true
    }
}
object MirrorShader {

  private val Include = """#pragma Mirror include "(.+)"""".r

  private[mirror] def missingShader(tpe: ShaderType) = MirrorShader(0, tpe)

  /**
    * Compiles an individual shader. If you use this method, you're on your own.
    * @param location The location of the shader.
    * @param shaderType The shader type to compile.
    * @param resourceManager A resource manager to load the shader from.
    */
  @throws[ShaderException]
  @throws[IOException]
  def compile(
      location: ResourceLocation,
      shaderType: ShaderType,
      resourceManager: IResourceManager
  ): MirrorShader = {
    val shaderSource = parseShaderSource(location, resourceManager).mkString("\n")

    val buffer = BufferUtils.createByteBuffer(shaderSource.length)
    buffer.put(shaderSource.getBytes)
    buffer.flip()
    val shaderId = OpenGlHelper.glCreateShader(shaderType.constant)
    OpenGlHelper.glShaderSource(shaderId, buffer)
    OpenGlHelper.glCompileShader(shaderId)

    if (OpenGlHelper.glGetShaderi(shaderId, OpenGlHelper.GL_COMPILE_STATUS) == 0) {
      val s = StringUtils.trim(OpenGlHelper.glGetShaderInfoLog(shaderId, 32768))
      throw new ShaderException(s"Couldn't compile $location\nError: $s")
    }
    MirrorShader(shaderId, shaderType)
  }

  @throws[IOException]
  private[mirror] def parseShaderSource(location: ResourceLocation, resourceManager: IResourceManager): Seq[String] = {
    var resource: IResource = null
    try {
      resource = resourceManager.getResource(location)
      val lines = IOUtils.readLines(resource.getInputStream, "UTF-8").asScala
      lines.flatMap {
        case Include(file) =>
          val includeLocation = if (file.contains(':')) {
            new ResourceLocation(file)
          } else {
            resource.getInputStream
            val path      = location.getPath
            val folderIdx = path.lastIndexOf('/')
            if (folderIdx != -1) {
              val folder = path.substring(0, folderIdx)
              new ResourceLocation(location.getNamespace, s"$folder/$file")
            } else {
              new ResourceLocation(location.getNamespace, file)
            }
          }

          parseShaderSource(includeLocation, resourceManager)
        case other => Seq(other)
      }
    } finally {
      IOUtils.closeQuietly(resource)
    }
  }
}
