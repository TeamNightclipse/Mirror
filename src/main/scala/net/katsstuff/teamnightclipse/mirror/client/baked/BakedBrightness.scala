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
import javax.annotation.Nullable

import net.katsstuff.teamnightclipse.mirror.client.ClientProxy
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
