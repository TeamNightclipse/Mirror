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

import scala.language.dynamics

import org.lwjgl.util.vector.{Matrix2f, Matrix3f, Matrix4f}

class UniformSyntax(val program: MirrorShaderProgram) extends AnyVal with Dynamic {

  def selectDynamic[A <: UniformType](name: String)(implicit mkSyntax: UniformTypeMkSyntax[A], tpe: A): mkSyntax.Syntax = {
    val uniform = program.getUniform(name).filter(_.tpe == tpe).getOrElse(new NOOPUniform(tpe, 1))
    mkSyntax.mkSyntax(uniform)
  }
}
sealed trait UniformTypeMkSyntax[A] {
  type Syntax
  def mkSyntax(uniform: MirrorUniform): Syntax
}
object UniformTypeMkSyntax {
  type Aux[A, Syntax0] = UniformTypeMkSyntax[A] { type Syntax = Syntax0 }

  trait UniformSyntaxCommon extends Any {
    def uniform: MirrorUniform
    def upload(): Unit = uniform.upload()
  }

  class IntSyntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(i: Int): Unit = uniform.set(i)
    def setIdx(i: Int, idx: Int): Unit = uniform.setIdx(i, idx)
  }

  class IVec2Syntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(i1: Int, i2: Int): Unit = uniform.set(i1, i2)
    def setIdx(i1: Int, i2: Int, idx: Int): Unit = uniform.setIdx(i1, i2, idx)
  }

  class IVec3Syntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(i1: Int, i2: Int, i3: Int): Unit = uniform.set(i1, i2, i3)
    def setIdx(i1: Int, i2: Int, i3: Int, idx: Int): Unit = uniform.setIdx(i1, i2, i3, idx)
  }

  class IVec4Syntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(i1: Int, i2: Int, i3: Int, i4: Int): Unit = uniform.set(i1, i2, i3, i4)
    def setIdx(i1: Int, i2: Int, i3: Int, i4: Int, idx: Int): Unit = uniform.setIdx(i1, i2, i3, i4, idx)
  }

  class FloatSyntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(f: Float): Unit = uniform.set(f)
    def setIdx(f: Float, idx: Int): Unit = uniform.setIdx(f, idx)
  }

  class Vec2Syntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(f1: Float, f2: Float): Unit = uniform.set(f1, f2)
    def setIdx(f1: Float, f2: Float, idx: Int): Unit = uniform.setIdx(f1, f2, idx)
  }

  class Vec3Syntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(f1: Float, f2: Float, f3: Float): Unit = uniform.set(f1, f2, f3)
    def setIdx(f1: Float, f2: Float, f3: Float, idx: Int): Unit = uniform.setIdx(f1, f2, f3, idx)
  }

  class Vec4Syntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(f1: Float, f2: Float, f3: Float, f4: Float): Unit = uniform.set(f1, f2, f3, f4)
    def setIdx(f1: Float, f2: Float, f3: Float, f4: Float, idx: Int): Unit = uniform.setIdx(f1, f2, f3, f4, idx)
  }

  class Mat2Syntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(
        m00: Float,
        m01: Float,
        m10: Float,
        m11: Float
    ): Unit = uniform.set(m00, m01, m10, m11)

    def set(matrix2f: Matrix2f): Unit = uniform.set(matrix2f)

    def setIdx(
        m00: Float,
        m01: Float,
        m10: Float,
        m11: Float,
        idx: Int
    ): Unit = uniform.setIdx(m00, m01, m10, m11, idx)

    def setIdx(matrix2f: Matrix2f, idx: Int): Unit = uniform.setIdx(matrix2f, idx)
  }

  class Mat3Syntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(
        m00: Float,
        m01: Float,
        m02: Float,
        m10: Float,
        m11: Float,
        m12: Float,
        m20: Float,
        m21: Float,
        m22: Float
    ): Unit = uniform.set(m00, m01, m02, m10, m11, m12, m20, m21, m22)

    def set(matrix3f: Matrix3f): Unit = uniform.set(matrix3f)

    def setIdx(
        m00: Float,
        m01: Float,
        m02: Float,
        m10: Float,
        m11: Float,
        m12: Float,
        m20: Float,
        m21: Float,
        m22: Float,
        idx: Int
    ): Unit = uniform.setIdx(m00, m01, m02, m10, m11, m12, m20, m21, m22, idx)

    def setIdx(matrix3f: Matrix3f, idx: Int): Unit = uniform.setIdx(matrix3f, idx)
  }

  class Mat4Syntax(val uniform: MirrorUniform) extends AnyVal with UniformSyntaxCommon {
    def set(
        m00: Float,
        m01: Float,
        m02: Float,
        m03: Float,
        m10: Float,
        m11: Float,
        m12: Float,
        m13: Float,
        m20: Float,
        m21: Float,
        m22: Float,
        m23: Float,
        m30: Float,
        m31: Float,
        m32: Float,
        m33: Float
    ): Unit = uniform.set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33)

    def set(matrix4f: Matrix4f): Unit = uniform.set(matrix4f)

    def setIdx(
        m00: Float,
        m01: Float,
        m02: Float,
        m03: Float,
        m10: Float,
        m11: Float,
        m12: Float,
        m13: Float,
        m20: Float,
        m21: Float,
        m22: Float,
        m23: Float,
        m30: Float,
        m31: Float,
        m32: Float,
        m33: Float,
        idx: Int
    ): Unit = uniform.setIdx(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33, idx)

    def set(matrix4f: Matrix4f, idx: Int): Unit = uniform.setIdx(matrix4f, idx)
  }

  implicit val intSyntax: Aux[UniformType.UnInt, IntSyntax] = new UniformTypeMkSyntax[UniformType.UnInt] {
    override type Syntax = IntSyntax
    override def mkSyntax(uniform: MirrorUniform): IntSyntax = new IntSyntax(uniform)
  }

  implicit val iVec2Syntax: Aux[UniformType.IVec2, IVec2Syntax] = new UniformTypeMkSyntax[UniformType.IVec2] {
    override type Syntax = IVec2Syntax
    override def mkSyntax(uniform: MirrorUniform): IVec2Syntax = new IVec2Syntax(uniform)
  }

  implicit val iVec3Syntax: Aux[UniformType.IVec3, IVec3Syntax] = new UniformTypeMkSyntax[UniformType.IVec3] {
    override type Syntax = IVec3Syntax
    override def mkSyntax(uniform: MirrorUniform): IVec3Syntax = new IVec3Syntax(uniform)
  }

  implicit val iVec4Syntax: Aux[UniformType.IVec4, IVec4Syntax] = new UniformTypeMkSyntax[UniformType.IVec4] {
    override type Syntax = IVec4Syntax
    override def mkSyntax(uniform: MirrorUniform): IVec4Syntax = new IVec4Syntax(uniform)
  }

  implicit val floatSyntax: Aux[UniformType.UnFloat, FloatSyntax] = new UniformTypeMkSyntax[UniformType.UnFloat] {
    override type Syntax = FloatSyntax
    override def mkSyntax(uniform: MirrorUniform): FloatSyntax = new FloatSyntax(uniform)
  }

  implicit val vec2Syntax: Aux[UniformType.Vec2, Vec2Syntax] = new UniformTypeMkSyntax[UniformType.Vec2] {
    override type Syntax = Vec2Syntax
    override def mkSyntax(uniform: MirrorUniform): Vec2Syntax = new Vec2Syntax(uniform)
  }

  implicit val vec3Syntax: Aux[UniformType.Vec3, Vec3Syntax] = new UniformTypeMkSyntax[UniformType.Vec3] {
    override type Syntax = Vec3Syntax
    override def mkSyntax(uniform: MirrorUniform): Vec3Syntax = new Vec3Syntax(uniform)
  }

  implicit val vec4Syntax: Aux[UniformType.Vec4, Vec4Syntax] = new UniformTypeMkSyntax[UniformType.Vec4] {
    override type Syntax = Vec4Syntax
    override def mkSyntax(uniform: MirrorUniform): Vec4Syntax = new Vec4Syntax(uniform)
  }

  implicit val mat2Syntax: Aux[UniformType.Mat2, Mat2Syntax] = new UniformTypeMkSyntax[UniformType.Mat2] {
    override type Syntax = Mat2Syntax
    override def mkSyntax(uniform: MirrorUniform): Mat2Syntax = new Mat2Syntax(uniform)
  }

  implicit val mat3Syntax: Aux[UniformType.Mat3, Mat3Syntax] = new UniformTypeMkSyntax[UniformType.Mat3] {
    override type Syntax = Mat3Syntax
    override def mkSyntax(uniform: MirrorUniform): Mat3Syntax = new Mat3Syntax(uniform)
  }

  implicit val mat4Syntax: Aux[UniformType.Mat4, Mat4Syntax] = new UniformTypeMkSyntax[UniformType.Mat4] {
    override type Syntax = Mat4Syntax
    override def mkSyntax(uniform: MirrorUniform): Mat4Syntax = new Mat4Syntax(uniform)
  }
}
