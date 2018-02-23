package net.katsstuff.mirror.client.baked

import java.util

import javax.annotation.Nullable

import scala.collection.JavaConverters._

import net.katsstuff.mirror.client.ClientProxy
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.{BakedQuad, ItemOverrideList}
import net.minecraft.client.renderer.vertex.{DefaultVertexFormats, VertexFormat}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
abstract class BakedBrightness(formatTemplate: VertexFormat) extends BakedPerspective {
  val format = new VertexFormat(formatTemplate)

  if (!ClientProxy.isOptifineInstalled) format.addElement(DefaultVertexFormats.TEX_2S)

  override def getQuads(@Nullable state: IBlockState, @Nullable facing: EnumFacing, rand: Long): util.List[BakedQuad] =
    if (facing != null) Nil.asJava
    else getQuads(state, if (state == null) DefaultVertexFormats.ITEM else format)

  def getQuads(@Nullable state: IBlockState, format: VertexFormat): util.List[BakedQuad]

  override def isAmbientOcclusion = true

  override def isGui3d = false

  override def isBuiltInRenderer = false

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
