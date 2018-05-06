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

import org.lwjgl.opengl.{ARBBufferObject, GL15, GLContext}

import net.minecraft.client.renderer.OpenGlHelper
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * A extension of the [[OpenGlHelper]] provided by minecraft. Contains extra constants and helpers.
  */
@SideOnly(Side.CLIENT)
object MirrorOpenGLHelper {

  private val contextcapabilities = GLContext.getCapabilities
  private val arbVbo              = !contextcapabilities.OpenGL15 && contextcapabilities.GL_ARB_vertex_buffer_object

  val GL_STREAM_DRAW: Int  = assignVboValue(GL15.GL_STREAM_DRAW, ARBBufferObject.GL_STREAM_DRAW_ARB)
  val GL_STREAM_READ: Int  = assignVboValue(GL15.GL_STREAM_READ, ARBBufferObject.GL_STREAM_READ_ARB)
  val GL_STREAM_COPY: Int  = assignVboValue(GL15.GL_STREAM_COPY, ARBBufferObject.GL_STREAM_COPY_ARB)
  val GL_STATIC_DRAW: Int  = assignVboValue(GL15.GL_STATIC_DRAW, ARBBufferObject.GL_STATIC_DRAW_ARB)
  val GL_STATIC_READ: Int  = assignVboValue(GL15.GL_STATIC_READ, ARBBufferObject.GL_STATIC_READ_ARB)
  val GL_STATIC_COPY: Int  = assignVboValue(GL15.GL_STATIC_COPY, ARBBufferObject.GL_STATIC_COPY_ARB)
  val GL_DYNAMIC_DRAW: Int = assignVboValue(GL15.GL_DYNAMIC_DRAW, ARBBufferObject.GL_DYNAMIC_DRAW_ARB)
  val GL_DYNAMIC_READ: Int = assignVboValue(GL15.GL_DYNAMIC_READ, ARBBufferObject.GL_DYNAMIC_READ_ARB)
  val GL_DYNAMIC_COPY: Int = assignVboValue(GL15.GL_DYNAMIC_COPY, ARBBufferObject.GL_DYNAMIC_COPY_ARB)

  private def assignVboValue(glValue: Int, arbValue: Int): Int =
    if (OpenGlHelper.vboSupported) if (arbVbo) arbValue else glValue else 0
}
