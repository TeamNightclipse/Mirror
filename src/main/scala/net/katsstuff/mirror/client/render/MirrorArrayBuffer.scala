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
package net.katsstuff.mirror.client.render

import java.nio.ByteBuffer

import org.lwjgl.opengl.{GL11, GL15}

import net.minecraft.client.renderer.{GlStateManager, OpenGlHelper}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Lightly abstracts over buffers in OpenGL.
  */
@SideOnly(Side.CLIENT)
class MirrorArrayBuffer(count: Int, target: Int, usage: Int) {
  private val id      = OpenGlHelper.glGenBuffers()
  private var deleted = false

  /**
    * Binds this buffer to OpenGL.
    */
  def bindBuffer(): Unit = {
    if (deleted) throw new IllegalStateException("Deleted")
    OpenGlHelper.glBindBuffer(target, id)
  }

  /**
    * Unbinds this buffer to OpenGL.
    */
  def unbindBuffer(): Unit = {
    if (deleted) throw new IllegalStateException("Deleted")
    OpenGlHelper.glBindBuffer(target, 0)
  }

  /**
    * Sets the data in this buffer.
    */
  def bufferData(data: ByteBuffer): Unit = {
    bindBuffer()
    OpenGlHelper.glBufferData(target, data, usage)
    unbindBuffer()
  }

  /**
    * Sets a portion of the data in this buffer.
    */
  def bufferSubData(offset: Int, data: ByteBuffer): Unit = {
    bindBuffer()
    GL15.glBufferSubData(target, offset, data)
    unbindBuffer()
  }

  /**
    * Calls glDrawArrays with the content of this buffer.
    */
  def drawArrays(mode: Int): Unit = {
    if (deleted) throw new IllegalStateException("Deleted")
    GlStateManager.glDrawArrays(mode, 0, count)
  }

  /**
    * Calls glDrawElements with the content of this buffer.
    */
  def drawElements(mode: Int, tpe: Int, offset: Int): Unit = {
    if (deleted) throw new IllegalStateException("Deleted")
    bindBuffer()
    GL11.glDrawElements(mode, count, tpe, offset)
  }

  /**
    * Deletes this buffer. Be careful about using this one.
    */
  def delete(): Unit =
    if (!deleted) {
      OpenGlHelper.glDeleteBuffers(id)
      deleted = true
    }
}
