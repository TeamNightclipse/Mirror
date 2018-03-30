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
package net.katsstuff.mirror.network.scalachannel

import net.katsstuff.mirror.data.Vector3
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.network.NetworkRegistry

/**
  * A custom target point used by the scala network wrapper. Should hopefully be easier to work with.
  * @param dimension The dimension id this targets.
  * @param x The x coordinate this targets.
  * @param y The y coordinate this targets.
  * @param z The z coordinate this targets.
  * @param range The range of this point.
  */
case class TargetPoint(dimension: Int, x: Double, y: Double, z: Double, range: Double) {

  /**
    * Converts this point to the forge variant.
    * @return
    */
  def toForge: NetworkRegistry.TargetPoint = new NetworkRegistry.TargetPoint(dimension, x, y, z, range)
}
object TargetPoint {

  /**
    * Creates a target point around an entity.
    * @param entity The entity to target around.
    * @param range The range around the entity.
    */
  def around(entity: Entity, range: Double): TargetPoint =
    TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, range)

  /**
    * Creates a target point around a vector.
    * @param dimension The dimension id to send to.
    * @param pos The vector position to send around.
    * @param range The radius of the target point.
    */
  def around(dimension: Int, pos: Vector3, range: Double): TargetPoint =
    TargetPoint(dimension, pos.x, pos.y, pos.z, range)
}
