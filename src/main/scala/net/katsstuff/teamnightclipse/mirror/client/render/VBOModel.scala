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
package net.katsstuff.teamnightclipse.mirror.client.render

import scala.collection.JavaConverters._

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._

import net.katsstuff.teamnightclipse.mirror.helper.MirrorLogHelper
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage._
import net.minecraft.client.renderer.vertex.{VertexFormat, VertexFormatElement}
import net.minecraft.client.renderer.{GlStateManager, OpenGlHelper}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Represents a VBO copy of a model with rendering wrapped around it.
  * @param format The [[VertexFormat]] to use when rendering the model.
  * @param arrayBuffer The place where all the rendering data is stored.
  * @param vertexCount The vertex count of the model.
  * @param mode The rendering mode. (drawArrays)
  */
//Mix of WorldVertexBufferUploader and ForgeHooksClient
@SideOnly(Side.CLIENT)
case class VBOModel(format: VertexFormat, arrayBuffer: MirrorArrayBuffer, vertexCount: Int, mode: Int) {

  /**
    * Draws this model.
    */
  def draw(): Unit = {
    if (vertexCount > 0) {
      val elements = format.getElements.asScala
      arrayBuffer.bindBuffer()

      for (i <- elements.indices) {
        preDraw(elements(i).getUsage, format, i, format.getElementCount)
      }

      arrayBuffer.drawArrays(mode)
      arrayBuffer.unbindBuffer()

      for (i <- elements.indices) {
        postDraw(elements(i).getUsage, format, i)
      }
    }
  }

  private def preDraw(usage: VertexFormatElement.EnumUsage, format: VertexFormat, element: Int, stride: Int): Unit = {
    val attr   = format.getElement(element)
    val count  = attr.getElementCount
    val tpe    = attr.getType.getGlConstant
    val offset = format.getOffset(element)
    usage match {
      case POSITION =>
        glVertexPointer(count, tpe, stride, offset)
        glEnableClientState(GL_VERTEX_ARRAY)
      case NORMAL =>
        if (count != 3) throw new IllegalArgumentException("Normal attribute should have the size 3: " + attr)
        glNormalPointer(tpe, stride, offset)
        glEnableClientState(GL_NORMAL_ARRAY)
      case COLOR =>
        glColorPointer(count, tpe, stride, offset)
        glEnableClientState(GL_COLOR_ARRAY)
      case UV =>
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + attr.getIndex)
        glTexCoordPointer(count, tpe, stride, offset)
        glEnableClientState(GL_TEXTURE_COORD_ARRAY)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
      case PADDING =>
      case GENERIC =>
        glEnableVertexAttribArray(attr.getIndex)
        glVertexAttribPointer(attr.getIndex, count, tpe, false, stride, offset)
      case _ => MirrorLogHelper.fatal(s"Unimplemented vanilla attribute upload: ${usage.getDisplayName}")
    }
  }

  private def postDraw(usage: VertexFormatElement.EnumUsage, format: VertexFormat, element: Int): Unit = {
    val attr = format.getElement(element)
    usage match {
      case POSITION =>
        glDisableClientState(GL_VERTEX_ARRAY)
      case NORMAL =>
        glDisableClientState(GL_NORMAL_ARRAY)
      case COLOR =>
        glDisableClientState(GL_COLOR_ARRAY)
        // is this really needed?
        GlStateManager.resetColor()
      case UV =>
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + attr.getIndex)
        glDisableClientState(GL_TEXTURE_COORD_ARRAY)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
      case PADDING =>
      case GENERIC =>
        glDisableVertexAttribArray(attr.getIndex)
      case _ => MirrorLogHelper.fatal(s"Unimplemented vanilla attribute upload: ${usage.getDisplayName}")
    }
  }
}
