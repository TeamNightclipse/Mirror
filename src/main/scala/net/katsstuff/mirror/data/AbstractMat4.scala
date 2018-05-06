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
package net.katsstuff.mirror.data

sealed trait AbstractMat4 { self =>
  type Self <: AbstractMat4 { type Self = self.Self }

  def m00: Double
  def m01: Double
  def m02: Double
  def m03: Double

  def m10: Double
  def m11: Double
  def m12: Double
  def m13: Double

  def m20: Double
  def m21: Double
  def m22: Double
  def m23: Double

  def m30: Double
  def m31: Double
  def m32: Double
  def m33: Double

  // format: OFF
  def create(
      m00: Double, m01: Double, m02: Double, m03: Double,
      m10: Double, m11: Double, m12: Double, m13: Double,
      m20: Double, m21: Double, m22: Double, m23: Double,
      m30: Double, m31: Double, m32: Double, m33: Double
  ): Self
  // format: ON

  // format: OFF
  def transpose: Self = create(
    m00 = m00, m01 = m10, m02 = m20, m03 = m30,
    m10 = m01, m11 = m11, m12 = m21, m13 = m31,
    m20 = m02, m21 = m12, m22 = m22, m23 = m32,
    m30 = m03, m31 = m13, m32 = m23, m33 = m33
  )

  def *(scalar: Double): Self = create(
    m00 = m00 * scalar, m01 = m01 * scalar, m02 = m02 * scalar, m03 = m03 * scalar,
    m10 = m10 * scalar, m11 = m11 * scalar, m12 = m12 * scalar, m13 = m13 * scalar,
    m20 = m20 * scalar, m21 = m21 * scalar, m22 = m22 * scalar, m23 = m23 * scalar,
    m30 = m30 * scalar, m31 = m31 * scalar, m32 = m32 * scalar, m33 = m33 * scalar
  )
  // format: ON

  def multiplyScalar(scalar: Double): Self = this * scalar

  def transformDirection(vec: AbstractVector3): vec.Self = {
    val x = vec.x
    val y = vec.y
    val z = vec.z

    // format: OFF
    vec.create(
      m00 * x + m10 * y + m20 * z,
      m01 * x + m11 * y + m21 * z,
      m02 * x + m12 * y + m22 * z
    )
    // format: ON
  }
}
final case class MutableMat4(
    var m00: Double,
    var m01: Double,
    var m02: Double,
    var m03: Double,
    var m10: Double,
    var m11: Double,
    var m12: Double,
    var m13: Double,
    var m20: Double,
    var m21: Double,
    var m22: Double,
    var m23: Double,
    var m30: Double,
    var m31: Double,
    var m32: Double,
    var m33: Double
) extends AbstractMat4 {

  override type Self = MutableMat4

  // format: OFF
  override def create(
      m00: Double, m01: Double, m02: Double, m03: Double,
      m10: Double, m11: Double, m12: Double, m13: Double,
      m20: Double, m21: Double, m22: Double, m23: Double,
      m30: Double, m31: Double, m32: Double, m33: Double
  ): Self = MutableMat4(
    m00, m01, m02, m03,
    m10, m11, m12, m13,
    m20, m21, m22, m23,
    m30, m31, m32, m33
  )
  // format: ON

  def *=(scalar: Double): Unit = {
    m00 *= scalar
    m01 *= scalar
    m02 *= scalar
    m03 *= scalar
    m10 *= scalar
    m11 *= scalar
    m12 *= scalar
    m13 *= scalar
    m20 *= scalar
    m21 *= scalar
    m22 *= scalar
    m23 *= scalar
    m30 *= scalar
    m31 *= scalar
    m32 *= scalar
    m33 *= scalar
  }

  def multiplyScalarMutable(scalar: Double): Unit = this *= scalar

  override def transpose: MutableMat4                      = super.transpose
  override def multiplyScalar(scalar: Double): MutableMat4 = super.multiplyScalar(scalar)
  def copyObj: MutableMat4                                 = copy()
}

// format: OFF
final case class Mat4(
    m00: Double, m01: Double, m02: Double, m03: Double,
    m10: Double, m11: Double, m12: Double, m13: Double,
    m20: Double, m21: Double, m22: Double, m23: Double,
    m30: Double, m31: Double, m32: Double, m33: Double
) extends AbstractMat4 {
  // format: ON
  override type Self = Mat4

  override def create(
      m00: Double,
      m01: Double,
      m02: Double,
      m03: Double,
      m10: Double,
      m11: Double,
      m12: Double,
      m13: Double,
      m20: Double,
      m21: Double,
      m22: Double,
      m23: Double,
      m30: Double,
      m31: Double,
      m32: Double,
      m33: Double
  ): Self = Mat4(
    m00,
    m01,
    m02,
    m03,
    m10,
    m11,
    m12,
    m13,
    m20,
    m21,
    m22,
    m23,
    m30,
    m31,
    m32,
    m33
  )

  override def transpose: Mat4                      = super.transpose
  override def multiplyScalar(scalar: Double): Mat4 = super.multiplyScalar(scalar)
}
object Mat4 {

  // format: ON
  val Identity = Mat4(
    1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1
  )
  // format: OFF

  //From libgdx https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Matrix4.java#L876
  def fromWorld(pos: AbstractVector3, forward: AbstractVector3, up: AbstractVector3): Mat4 = {
    val forwardNormalized = forward.normalize
    val right             = forwardNormalized.cross(up).normalize
    val newUp             = right.cross(forwardNormalized).normalize

    fromAxes(right, newUp, forwardNormalized, pos)
  }

  def fromAxes(xAxis: AbstractVector3, yAxis: AbstractVector3, zAxis: AbstractVector3, pos: AbstractVector3): Mat4 =
    // format: OFF
    Mat4(
      xAxis.x, xAxis.y, xAxis.z, pos.x,
      yAxis.x, yAxis.y, yAxis.z, pos.y,
      zAxis.x, zAxis.y, zAxis.z, pos.z,
      0,       0,       0,       1
    )
    // format: ON
}
