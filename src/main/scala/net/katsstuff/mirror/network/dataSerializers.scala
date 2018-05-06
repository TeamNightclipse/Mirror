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
package net.katsstuff.mirror.network

import net.katsstuff.mirror.data.Vector3
import net.minecraft.network.PacketBuffer
import net.minecraft.network.datasync.{DataParameter, DataSerializer}

class OptionSerializers[A](serializer: DataSerializer[A]) extends DataSerializer[Option[A]] {

  override def write(buf: PacketBuffer, value: Option[A]): Unit = value match {
    case Some(present) =>
      buf.writeBoolean(true)
      serializer.write(buf, present)
    case None => buf.writeBoolean(false)
  }
  override def read(buf: PacketBuffer): Option[A] =
    if (buf.readBoolean()) {
      Some(serializer.read(buf))
    } else None

  override def createKey(id: Int): DataParameter[Option[A]] = new DataParameter(id, this)
  override def copyValue(value: Option[A]): Option[A]       = value.map(serializer.copyValue)
}

class SeqSerializer[A](serializer: DataSerializer[A]) extends DataSerializer[Seq[A]] {
  override def write(buf: PacketBuffer, value: Seq[A]): Unit = {
    buf.writeInt(value.size)
    value.foreach(serializer.write(buf, _))
  }

  override def read(buf: PacketBuffer): Seq[A] = {
    val size = buf.readInt()
    for (_ <- 0 until size) yield serializer.read(buf)
  }

  override def createKey(id: Int): DataParameter[Seq[A]] = new DataParameter(id, this)
  override def copyValue(value: Seq[A]): Seq[A]          = value.map(serializer.copyValue)
}

object Vector3Serializer extends DataSerializer[Vector3] {
  override def write(buf: PacketBuffer, value: Vector3): Unit = {
    buf.writeDouble(value.x)
    buf.writeDouble(value.y)
    buf.writeDouble(value.z)
  }
  override def read(buf: PacketBuffer): Vector3           = Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble())
  override def createKey(id: Int): DataParameter[Vector3] = new DataParameter(id, this)
  override def copyValue(value: Vector3): Vector3         = value
}

class EnumSerializer[T <: Enum[T]](val enumClass: Class[T]) extends DataSerializer[T] {
  override def write(buf: PacketBuffer, value: T): Unit = buf.writeEnumValue(value)
  override def read(buf: PacketBuffer): T               = buf.readEnumValue(enumClass)
  override def createKey(id: Int)                       = new DataParameter[T](id, this)
  override def copyValue(value: T): T                   = value
}
