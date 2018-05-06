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
package net.katsstuff.mirror.scalastuff

import java.util.Optional
import java.util.function.{
  BooleanSupplier,
  Consumer => JConsumer,
  Function => JFunction,
  Predicate => JPredicate,
  Supplier => JSupplier
}

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

import com.google.common.base.{Function => GFunction, Predicate => GPredicate, Supplier => GSupplier}

import net.minecraft.entity.Entity
import net.minecraft.network.PacketBuffer
import net.minecraft.network.datasync.{DataParameter, DataSerializer}
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.World

object MirrorImplicits {

  implicit class RichFunction0[A](val function: () => A) extends AnyVal {
    def asJava: JSupplier[A]  = () => function()
    def asGuava: GSupplier[A] = () => function()
  }

  implicit class RichFunction1[A, B](val function1: A => B) extends AnyVal {
    def asJava: JFunction[A, B]  = a => function1(a)
    def asGuava: GFunction[A, B] = a => function1(a)
  }

  implicit class RichFunction1Boolean[A](val function1: A => Boolean) extends AnyVal {
    def asJava: JPredicate[A]  = a => function1(a)
    def asGuava: GPredicate[A] = a => function1(a)
  }

  implicit class RichFunction1Unit[A](val function1: A => Unit) extends AnyVal {
    def asJava: JConsumer[A] = a => function1(a)
  }

  implicit class RichSupplier[A](val supplier: JSupplier[A]) extends AnyVal {
    def asScala: () => A = () => supplier.get()
  }

  implicit class RichBooleanSupplier(val supplier: BooleanSupplier) extends AnyVal {
    def asScala: () => Boolean = () => supplier.getAsBoolean
  }

  implicit class RichJFunction[A, B](val function: JFunction[A, B]) extends AnyVal {
    def asScala: A => B = a => function(a)
  }

  implicit class RichPredicate[A](val function: JPredicate[A]) extends AnyVal {
    def asScala: A => Boolean = a => function.test(a)
  }

  implicit class RichConsumer[A](val function: JConsumer[A]) extends AnyVal {
    def asScala: A => Unit = a => function.accept(a)
  }

  implicit class RichOptional[A](val optional: Optional[A]) extends AnyVal {

    def toOption: Option[A] = if (optional.isPresent) Some(optional.get()) else None
  }

  implicit class RichOption[A](val option: Option[A]) extends AnyVal {

    def toOptional: Optional[A] = option.fold(Optional.empty[A])(Optional.of)
  }

  implicit class RichDataSerializer[A](val dataSerializer: DataSerializer[A]) extends AnyVal {
    def transform[B](to: A => B, from: B => A): DataSerializer[B] = new DataSerializer[B] {
      override def read(buf: PacketBuffer): B = to(dataSerializer.read(buf))

      override def createKey(id: Int): DataParameter[B] = new DataParameter[B](id, this)

      override def copyValue(value: B): B = to(dataSerializer.copyValue(from(value)))

      override def write(buf: PacketBuffer, value: B): Unit = dataSerializer.write(buf, from(value))
    }
  }

  implicit class RichWorld(val world: World) extends AnyVal {
    def entitiesWithinAABBExcludingEntity[A](entity: Option[Entity], boundingBox: AxisAlignedBB): Seq[Entity] =
      world.getEntitiesInAABBexcluding(entity.orNull, boundingBox, null).asScala

    def collectEntitiesWithinAABBExcludingEntity[A](entity: Option[Entity], boundingBox: AxisAlignedBB)(
        pf: PartialFunction[Entity, A]
    ): Seq[A] =
      world
        .getEntitiesInAABBexcluding(entity.orNull, boundingBox, (pf.isDefinedAt _).asGuava)
        .asScala
        .map(pf)

    def collectEntities[A <: Entity, B](pf: PartialFunction[A, B])(implicit classTag: ClassTag[A]): Seq[B] =
      world.getEntities(classTag.runtimeClass.asInstanceOf[Class[A]], (pf.isDefinedAt _).asGuava).asScala.map(pf)
    def collectPlayers[A <: Entity, B](pf: PartialFunction[A, B])(implicit classTag: ClassTag[A]): Seq[B] =
      world.getPlayers(classTag.runtimeClass.asInstanceOf[Class[A]], (pf.isDefinedAt _).asGuava).asScala.map(pf)

    def entitiesWithinAABB[A <: Entity](boundingBox: AxisAlignedBB)(implicit classTag: ClassTag[A]): Seq[A] =
      world.getEntitiesWithinAABB(classTag.runtimeClass.asInstanceOf[Class[A]], boundingBox).asScala

    def collectEntitiesWithinAABB[A <: Entity, B](
        boundingBox: AxisAlignedBB
    )(pf: PartialFunction[A, B])(implicit classTag: ClassTag[A]): Seq[B] =
      world
        .getEntitiesWithinAABB(classTag.runtimeClass.asInstanceOf[Class[A]], boundingBox, (pf.isDefinedAt _).asGuava)
        .asScala
        .map(pf)

    def findNearestEntityWithinAABB[A <: Entity](closestTo: A, boundingBox: AxisAlignedBB)(
        implicit classTag: ClassTag[A]
    ): Option[A] =
      Option(world.findNearestEntityWithinAABB(classTag.runtimeClass.asInstanceOf[Class[A]], boundingBox, closestTo))
  }
}
