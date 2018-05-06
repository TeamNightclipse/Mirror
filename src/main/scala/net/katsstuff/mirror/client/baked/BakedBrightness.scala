package net.katsstuff.mirror.client.baked

import java.util
import javax.annotation.Nullable

import net.katsstuff.mirror.client.ClientProxy
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.{BakedQuad, ItemOverrideList}
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.{DefaultVertexFormats, VertexFormat}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConverters._

@SideOnly(Side.CLIENT)
abstract class BakedBrightness extends BakedPerspective {
  var format: VertexFormat = _

  override def applyFormat(format: VertexFormat): Baked = {
    if (!ClientProxy.isOptifineInstalled) {
      this.format = new VertexFormat(format)
      this.format.addElement(DefaultVertexFormats.TEX_2S)
    } else this.format = format
    this
  }

  override def getQuads(@Nullable state: IBlockState, @Nullable facing: EnumFacing, rand: Long): util.List[BakedQuad] =
    if (facing != null) Nil.asJava
    else getQuads(state)

  def getQuads(@Nullable state: IBlockState): util.List[BakedQuad]

  override def isAmbientOcclusion = true

  override def isGui3d = false

  override def isBuiltInRenderer = false

  override def getParticleTexture: TextureAtlasSprite =
    Minecraft.getMinecraft.getTextureMapBlocks.getTextureExtry(getParticle.toString)

  def getParticle: ResourceLocation

  override def getOverrides: ItemOverrideList = {
    new ItemOverrideList(Nil.asJava) {
      @Nullable
      override def applyOverride(
          stack: ItemStack,
          @Nullable worldIn: World,
          @Nullable entityIn: EntityLivingBase
      ): ResourceLocation = null
    }
  }
}
