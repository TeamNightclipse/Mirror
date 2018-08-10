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
package net.katsstuff.teamnightclipse.mirror.client.baked

import java.util
import javax.vecmath.{Matrix4f, Vector3f}

import net.minecraft.client.renderer.block.model.{IBakedModel, ItemCameraTransforms}
import net.minecraftforge.common.model.TRSRTransformation
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConverters._

@SideOnly(Side.CLIENT)
object BakedPerspective {
  val BlockTransforms: Map[ItemCameraTransforms.TransformType, TRSRTransformation] = Map(
    ItemCameraTransforms.TransformType.GUI                     -> mkTransform(0F, 0F, 0F, 30F, 45F, 0F, 0.63F),
    ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND -> mkTransform(0F, 2.5F, 0F, 75F, 45F, 0F, 0.38F),
    ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND  -> mkTransform(0F, 2.5F, 0F, 75F, 45F, 0F, 0.38F),
    ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND -> mkTransform(0F, 0F, 0F, 0F, 45F, 0F, 0.38F),
    ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND  -> mkTransform(0F, 0F, 0F, 0F, 225F, 0F, 0.38F),
    ItemCameraTransforms.TransformType.GROUND                  -> mkTransform(0F, 3.5F, 0F, 0F, 0F, 0F, 0.25F),
    ItemCameraTransforms.TransformType.FIXED                   -> mkTransform(0F, 1F, 0F, 0F, 0F, 0F, 0.5F)
  )

  val BlockTransformsJava: util.Map[ItemCameraTransforms.TransformType, TRSRTransformation] = BlockTransforms.asJava

  private val DefaultTransform = mkTransform(0, 0, 0, 0, 0, 0, 1)

  def mkTransform(tx: Float, ty: Float, tz: Float, ax: Float, ay: Float, az: Float, size: Float) =
    new TRSRTransformation(
      new Vector3f(tx / 16, ty / 16, tz / 16),
      TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
      new Vector3f(size, size, size),
      null
    )
}
@SideOnly(Side.CLIENT)
abstract class BakedPerspective extends IBakedModel with Baked {

  import org.apache.commons.lang3.tuple.Pair

  override def handlePerspective(
      cameraTransformType: ItemCameraTransforms.TransformType
  ): Pair[_ <: IBakedModel, Matrix4f] =
    Pair.of(this, getTransforms.getOrElse(cameraTransformType, BakedPerspective.DefaultTransform).getMatrix)

  def getTransforms: Map[ItemCameraTransforms.TransformType, TRSRTransformation] =
    BakedPerspective.BlockTransforms

  def getTransformsJava: util.Map[ItemCameraTransforms.TransformType, TRSRTransformation] =
    BakedPerspective.BlockTransformsJava
}
