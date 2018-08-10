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
import java.util.function
import javax.annotation.Nullable

import scala.collection.JavaConverters._

import net.katsstuff.teamnightclipse.mirror.Mirror
import net.katsstuff.teamnightclipse.mirror.client.MirrorResourceHelper
import net.katsstuff.teamnightclipse.mirror.client.helper.ResourceHelperS
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

  private var particle: ResourceLocation = MirrorResourceHelper.getAtlas(None, "null")

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
