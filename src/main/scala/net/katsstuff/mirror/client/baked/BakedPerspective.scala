package net.katsstuff.mirror.client.baked

import java.util

import javax.vecmath.{Matrix4f, Vector3f}

import scala.collection.JavaConverters._

import net.minecraft.client.renderer.block.model.{IBakedModel, ItemCameraTransforms}
import net.minecraftforge.common.model.TRSRTransformation
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
object BakedPerspective {
  val BlockTransforms: Map[ItemCameraTransforms.TransformType, TRSRTransformation] = Map(
    ItemCameraTransforms.TransformType.GUI                     -> mkTransform(0F, 0F, 0F, 30F, 45F, 0F, 0.64F),
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
abstract class BakedPerspective extends IBakedModel {

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
