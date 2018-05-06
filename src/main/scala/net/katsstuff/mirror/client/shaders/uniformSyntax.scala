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

import scala.language.dynamics

import shapeless._
import shapeless.tag._
import org.lwjgl.util.vector.{Matrix2f, Matrix3f, Matrix4f}

/**
  * A class providing dynamic access to uniforms of a Shader program from Scala.
  */
class UniformSyntax[Uniforms <: HList](val typedProgram: MirrorShaderProgram.TypeLevelProgram[Uniforms])
    extends AnyVal
    with Dynamic {

  def selectDynamic[Type <: UniformType](name: String)(
      implicit
      select: ops.record.Selector.Aux[Uniforms, Symbol @@ name.type, Type],
      mkSyntax: UniformTypeMkSyntax[Type]
  ): mkSyntax.Syntax =
    mkSyntax(typedProgram.program.uniformMap(name).asInstanceOf[MirrorUniform[Type]])
}
sealed trait UniformTypeMkSyntax[Type <: UniformType] {
  type Syntax
  def apply(uniform: MirrorUniform[Type]): Syntax
}
object UniformTypeMkSyntax {
  type Aux[Type <: UniformType, Syntax0] = UniformTypeMkSyntax[Type] { type Syntax = Syntax0 }

  trait UniformSyntaxCommon[Type <: UniformType] extends Any {
    def uniform: MirrorUniform[Type]
    def upload(): Unit = uniform.upload()
  }

  class IntSyntax(val uniform: MirrorUniform[UniformType.UnInt])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.UnInt] {
    def set(i: Int): Unit              = uniform.set(i)
    def setIdx(i: Int, idx: Int): Unit = uniform.setIdx(i, idx)
  }

  class IVec2Syntax(val uniform: MirrorUniform[UniformType.IVec2])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.IVec2] {
    def set(i1: Int, i2: Int): Unit              = uniform.set(i1, i2)
    def setIdx(i1: Int, i2: Int, idx: Int): Unit = uniform.setIdx(i1, i2, idx)
  }

  class IVec3Syntax(val uniform: MirrorUniform[UniformType.IVec3])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.IVec3] {
    def set(i1: Int, i2: Int, i3: Int): Unit              = uniform.set(i1, i2, i3)
    def setIdx(i1: Int, i2: Int, i3: Int, idx: Int): Unit = uniform.setIdx(i1, i2, i3, idx)
  }

  class IVec4Syntax(val uniform: MirrorUniform[UniformType.IVec4])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.IVec4] {
    def set(i1: Int, i2: Int, i3: Int, i4: Int): Unit              = uniform.set(i1, i2, i3, i4)
    def setIdx(i1: Int, i2: Int, i3: Int, i4: Int, idx: Int): Unit = uniform.setIdx(i1, i2, i3, i4, idx)
  }

  class FloatSyntax(val uniform: MirrorUniform[UniformType.UnFloat])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.UnFloat] {
    def set(f: Float): Unit              = uniform.set(f)
    def setIdx(f: Float, idx: Int): Unit = uniform.setIdx(f, idx)
  }

  class Vec2Syntax(val uniform: MirrorUniform[UniformType.Vec2])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.Vec2] {
    def set(f1: Float, f2: Float): Unit              = uniform.set(f1, f2)
    def setIdx(f1: Float, f2: Float, idx: Int): Unit = uniform.setIdx(f1, f2, idx)
  }

  class Vec3Syntax(val uniform: MirrorUniform[UniformType.Vec3])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.Vec3] {
    def set(f1: Float, f2: Float, f3: Float): Unit              = uniform.set(f1, f2, f3)
    def setIdx(f1: Float, f2: Float, f3: Float, idx: Int): Unit = uniform.setIdx(f1, f2, f3, idx)
  }

  class Vec4Syntax(val uniform: MirrorUniform[UniformType.Vec4])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.Vec4] {
    def set(f1: Float, f2: Float, f3: Float, f4: Float): Unit              = uniform.set(f1, f2, f3, f4)
    def setIdx(f1: Float, f2: Float, f3: Float, f4: Float, idx: Int): Unit = uniform.setIdx(f1, f2, f3, f4, idx)
  }

  class Mat2Syntax(val uniform: MirrorUniform[UniformType.Mat2])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.Mat2] {
    def set(m00: Float, m01: Float, m10: Float, m11: Float): Unit = uniform.set(m00, m01, m10, m11)

    def set(matrix2f: Matrix2f): Unit = uniform.set(matrix2f)

    def setIdx(m00: Float, m01: Float, m10: Float, m11: Float, idx: Int): Unit = uniform.setIdx(m00, m01, m10, m11, idx)

    def setIdx(matrix2f: Matrix2f, idx: Int): Unit = uniform.setIdx(matrix2f, idx)
  }

  class Mat3Syntax(val uniform: MirrorUniform[UniformType.Mat3])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.Mat3] {
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

  class Mat4Syntax(val uniform: MirrorUniform[UniformType.Mat4])
      extends AnyVal
      with UniformSyntaxCommon[UniformType.Mat4] {
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

  implicit val intSyntax: Aux[UniformType.UnInt.type, IntSyntax] = new UniformTypeMkSyntax[UniformType.UnInt.type] {
    override type Syntax = IntSyntax
    override def apply(uniform: MirrorUniform[UniformType.UnInt.type]): IntSyntax = new IntSyntax(uniform)
  }

  implicit val iVec2Syntax: Aux[UniformType.IVec2.type, IVec2Syntax] = new UniformTypeMkSyntax[UniformType.IVec2.type] {
    override type Syntax = IVec2Syntax
    override def apply(uniform: MirrorUniform[UniformType.IVec2.type]): IVec2Syntax = new IVec2Syntax(uniform)
  }

  implicit val iVec3Syntax: Aux[UniformType.IVec3.type, IVec3Syntax] = new UniformTypeMkSyntax[UniformType.IVec3.type] {
    override type Syntax = IVec3Syntax
    override def apply(uniform: MirrorUniform[UniformType.IVec3.type]): IVec3Syntax = new IVec3Syntax(uniform)
  }

  implicit val iVec4Syntax: Aux[UniformType.IVec4.type, IVec4Syntax] = new UniformTypeMkSyntax[UniformType.IVec4.type] {
    override type Syntax = IVec4Syntax
    override def apply(uniform: MirrorUniform[UniformType.IVec4.type]): IVec4Syntax = new IVec4Syntax(uniform)
  }

  implicit val floatSyntax: Aux[UniformType.UnFloat.type, FloatSyntax] =
    new UniformTypeMkSyntax[UniformType.UnFloat.type] {
      override type Syntax = FloatSyntax
      override def apply(uniform: MirrorUniform[UniformType.UnFloat.type]): FloatSyntax = new FloatSyntax(uniform)
    }

  implicit val vec2Syntax: Aux[UniformType.Vec2.type, Vec2Syntax] = new UniformTypeMkSyntax[UniformType.Vec2.type] {
    override type Syntax = Vec2Syntax
    override def apply(uniform: MirrorUniform[UniformType.Vec2.type]): Vec2Syntax = new Vec2Syntax(uniform)
  }

  implicit val vec3Syntax: Aux[UniformType.Vec3.type, Vec3Syntax] = new UniformTypeMkSyntax[UniformType.Vec3.type] {
    override type Syntax = Vec3Syntax
    override def apply(uniform: MirrorUniform[UniformType.Vec3.type]): Vec3Syntax = new Vec3Syntax(uniform)
  }

  implicit val vec4Syntax: Aux[UniformType.Vec4.type, Vec4Syntax] = new UniformTypeMkSyntax[UniformType.Vec4.type] {
    override type Syntax = Vec4Syntax
    override def apply(uniform: MirrorUniform[UniformType.Vec4.type]): Vec4Syntax = new Vec4Syntax(uniform)
  }

  implicit val mat2Syntax: Aux[UniformType.Mat2.type, Mat2Syntax] = new UniformTypeMkSyntax[UniformType.Mat2.type] {
    override type Syntax = Mat2Syntax
    override def apply(uniform: MirrorUniform[UniformType.Mat2.type]): Mat2Syntax = new Mat2Syntax(uniform)
  }

  implicit val mat3Syntax: Aux[UniformType.Mat3.type, Mat3Syntax] = new UniformTypeMkSyntax[UniformType.Mat3.type] {
    override type Syntax = Mat3Syntax
    override def apply(uniform: MirrorUniform[UniformType.Mat3.type]): Mat3Syntax = new Mat3Syntax(uniform)
  }

  implicit val mat4Syntax: Aux[UniformType.Mat4.type, Mat4Syntax] = new UniformTypeMkSyntax[UniformType.Mat4.type] {
    override type Syntax = Mat4Syntax
    override def apply(uniform: MirrorUniform[UniformType.Mat4.type]): Mat4Syntax = new Mat4Syntax(uniform)
  }
}
