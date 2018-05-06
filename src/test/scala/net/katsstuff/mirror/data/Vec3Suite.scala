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

import org.junit.runner.RunWith
import org.scalacheck.Gen
import org.scalactic.{Equality, TolerantNumerics}
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import net.minecraft.util.math.MathHelper

@RunWith(classOf[JUnitRunner])
class Vec3Suite extends FunSuite with Matchers with GeneratorDrivenPropertyChecks {

  final val Epsilon = 1E5

  implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(1E-5)
  implicit val floatEquality: Equality[Float]   = TolerantNumerics.tolerantFloatEquality(1E-2.toFloat)

  val saneDouble: Gen[Double] = Gen.choose(-Epsilon, Epsilon)
  val angleFloat: Gen[Float]  = Gen.choose(0F, 360F)

  val randPos: Gen[Vector3] = for {
    x <- saneDouble
    y <- saneDouble
    z <- saneDouble
  } yield Vector3(x, y, z)

  val randDirection: Gen[Vector3] = for {
    yaw   <- angleFloat
    pitch <- angleFloat
  } yield Vector3.fromSpherical(yaw, pitch)

  val ex = Vector3(1, 0, 0)
  val ey = Vector3(0, 1, 0)
  val ez = Vector3(0, 0, 1)

  test("a + b = (ax + bx)ex + (ay + by)ey + (az + bz)ez") {
    forAll(randPos, randPos) { (a: Vector3, b: Vector3) =>
      a + b shouldEqual (a.x + b.x) * ex + (a.y + b.y) * ey + (a.z + b.z) * ez
    }
  }

  test("a - b = (ax - bx)ex + (ay - by)ey + (az - bz)ez") {
    forAll(randPos, randPos) { (a: Vector3, b: Vector3) =>
      a - b shouldEqual (a.x - b.x) * ex + (a.y - b.y) * ey + (a.z - b.z) * ez
    }
  }

  test("r * a = (r * ax)ex + (r * ay)ey + (r * az)ez") {
    forAll(saneDouble, randPos) { (r: Double, a: Vector3) =>
      r * a shouldEqual (r * a.x) * ex + (r * a.y) * ey + (r * a.z) * ez
    }
  }

  test("a dot b = |a| * |b| * cos theta") {
    forAll(randPos, randPos) { (a: Vector3, b: Vector3) =>
      a.dot(b) shouldEqual a.length * b.length * math.cos(a.angle(b))
    }
  }

  test("v1 * v2") {
    forAll(randPos, randPos) { (v1: Vector3, v2: Vector3) =>
      val product = v1 * v2

      (v1.x * v2.x) shouldBe product.x
      (v1.y * v2.y) shouldBe product.y
      (v1.z * v2.z) shouldBe product.z
    }
  }

  test("v1 / v2") {
    forAll(randPos, randPos) { (v1: Vector3, v2: Vector3) =>
      val quotient = v1 / v2

      (v1.x / v2.x) shouldBe quotient.x
      (v1.y / v2.y) shouldBe quotient.y
      (v1.z / v2.z) shouldBe quotient.z
    }
  }

  test("-v") {
    forAll(randPos) { v: Vector3 =>
      val negated = v.negate

      (-v.x) shouldBe negated.x
      (-v.y) shouldBe negated.y
      (-v.z) shouldBe negated.z
    }
  }

  test("Normalized vector v has |v| == 1") {
    forAll(randPos) { v: Vector3 =>
      v.normalize.length shouldEqual 1D
    }
  }

  test("|v * s| == |s|") {
    forAll(randDirection, saneDouble) { (v: Vector3, s: Double) =>
      whenever(s != 0) {
        (v * s).length shouldEqual math.abs(s) +- (math.abs(s) / 1E4)
      }
    }
  }

  test("Distance between v1 and v2 is |v2-v1|") {
    forAll(randPos, randPos) { (v1: Vector3, v2: Vector3) =>
      v1.distance(v2) shouldEqual (v2 - v1).length
    }
  }

  test("For vector v constructed from yaw1 and pitch1, the yaw and pitch of of v should be yaw1 and pitch1") {
    forAll(angleFloat, angleFloat) { (yaw: Float, pitch: Float) =>
      val v = Vector3.fromSpherical(yaw, pitch)

      def wrapPitch(value: Float): Float = {
        val modValue = value % 180F
        if (modValue >= 90.0D) modValue - 180F
        else if (modValue < -90.0D) modValue + 180F
        else modValue
      }

      v.yaw.toFloat shouldEqual MathHelper.wrapDegrees(yaw)
      v.pitch.toFloat shouldEqual wrapPitch(pitch)
    }
  }
}
