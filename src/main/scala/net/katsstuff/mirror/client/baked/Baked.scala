package net.katsstuff.mirror.client.baked

import java.util

import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.ResourceLocation

trait Baked extends IBakedModel {
  def getTextures: Array[ResourceLocation]
  def applyFormat(format: VertexFormat): Baked
  def applyTextures(sprites: util.function.Function[ResourceLocation, TextureAtlasSprite]): Baked
}
