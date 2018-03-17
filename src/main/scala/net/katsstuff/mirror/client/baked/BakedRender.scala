package net.katsstuff.mirror.client.baked

import java.util
import java.util.function
import javax.annotation.Nullable

import scala.collection.JavaConverters._

import net.katsstuff.mirror.Mirror
import net.katsstuff.mirror.client.helper.ResourceHelperS
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.{BakedQuad, ItemCameraTransforms, ItemOverrideList}
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.common.model.TRSRTransformation
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT) class BakedRender extends BakedPerspective {
  private var transforms = BakedPerspective.BlockTransforms

  private var particle: ResourceLocation = ResourceHelperS.getAtlas(Mirror.Id, None, "null")

  override def applyFormat(format: VertexFormat): Baked = this

  override def applyTextures(sprites: function.Function[ResourceLocation, TextureAtlasSprite]): Baked = this

  override def getTextures: Array[ResourceLocation] = Array.apply(getParticle)

  override def getQuads(@Nullable state: IBlockState, @Nullable side: EnumFacing, rand: Long): util.List[BakedQuad] =
    Nil.asJava

  override def isAmbientOcclusion = false

  override def isGui3d = false

  override def isBuiltInRenderer = true

  override def getParticleTexture: TextureAtlasSprite =
    Minecraft.getMinecraft.getTextureMapBlocks.getTextureExtry(getParticle.toString)

  def setTransformsJava(transforms: Map[ItemCameraTransforms.TransformType, TRSRTransformation]): BakedRender = {
    this.transforms = transforms
    this
  }

  def setTransformsJava(transforms: util.Map[ItemCameraTransforms.TransformType, TRSRTransformation]): BakedRender = {
    this.transforms = transforms.asScala.toMap
    this
  }

  override def getTransforms: Map[ItemCameraTransforms.TransformType, TRSRTransformation] = transforms

  override def getTransformsJava: util.Map[ItemCameraTransforms.TransformType, TRSRTransformation] = transforms.asJava

  def setParticle(particle: ResourceLocation): BakedRender = {
    this.particle = particle
    this
  }

  def getParticle: ResourceLocation = particle

  override def getOverrides = new ItemOverrideList(Nil.asJava)
}
