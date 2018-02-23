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
package net.katsstuff.mirror.client.particles

import scala.collection.mutable.ArrayBuffer

import org.lwjgl.opengl.GL11

import net.katsstuff.mirror.client.helper.Blending
import net.katsstuff.mirror.client.lib.LibParticleTexures
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{ActiveRenderInfo, GlStateManager, Tessellator}
import net.minecraftforge.client.event.{RenderWorldLastEvent, TextureStitchEvent}
import net.minecraftforge.fml.common.eventhandler.{EventPriority, SubscribeEvent}
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class ParticleRenderer {

  private val particles = ArrayBuffer.empty[IMirrorParticle]

  @SubscribeEvent
  def onTextureStitch(event: TextureStitchEvent): Unit = {
    event.getMap.registerSprite(LibParticleTexures.PARTICLE_GLINT)
    event.getMap.registerSprite(LibParticleTexures.PARTICLE_GLOW)
    event.getMap.registerSprite(LibParticleTexures.PARTICLE_MOTE)
    event.getMap.registerSprite(LibParticleTexures.PARTICLE_STAR)
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  def onTick(event: TickEvent.ClientTickEvent): Unit =
    if (event.side == Side.CLIENT && !Minecraft.getMinecraft.isGamePaused) updateParticles()

  @SubscribeEvent @SideOnly(Side.CLIENT)
  def onRenderAfterWorld(event: RenderWorldLastEvent): Unit = {
    GlStateManager.pushMatrix()
    renderParticles(event.getPartialTicks)
    GlStateManager.popMatrix()
  }

  private def updateParticles(): Unit = {
    val toRemove = for (particle <- particles) yield {
      if (particle.alive) {
        particle.onUpdateGlow()
        None
      } else Some(particle)
    }

    particles --= toRemove.flatten
  }

  private def renderParticles(partialTicks: Float): Unit = {
    val player = Minecraft.getMinecraft.player
    if (player != null) {
      val x  = ActiveRenderInfo.getRotationX
      val z  = ActiveRenderInfo.getRotationZ
      val yz = ActiveRenderInfo.getRotationYZ
      val xy = ActiveRenderInfo.getRotationXY
      val xz = ActiveRenderInfo.getRotationXZ

      Particle.interpPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
      Particle.interpPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
      Particle.interpPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks
      Particle.cameraViewDir = player.getLook(partialTicks)

      GlStateManager.enableAlpha()
      GlStateManager.enableBlend()
      GlStateManager.alphaFunc(516, 0.003921569F)
      GlStateManager.disableCull()
      GlStateManager.disableLighting()
      GlStateManager.depthMask(false)

      Minecraft.getMinecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
      val tess   = Tessellator.getInstance
      val buffer = tess.getBuffer

      Blending.Normal()
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
      for (particle <- particles if particle.shouldRender && !particle.isAdditive) {
        particle.renderParticleGlow(buffer, player, partialTicks, x, xz, z, yz, xy)
      }
      tess.draw()

      Blending.AdditiveAlpha()
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
      for (particle <- particles if particle.shouldRender && particle.isAdditive) {
        particle.renderParticleGlow(buffer, player, partialTicks, x, xz, z, yz, xy)
      }
      tess.draw()

      GlStateManager.disableDepth()
      Blending.Normal()
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
      for (particle <- particles if particle.shouldRender && particle.ignoreDepth && !particle.isAdditive) {
        particle.renderParticleGlow(buffer, player, partialTicks, x, xz, z, yz, xy)
      }
      tess.draw()

      Blending.AdditiveAlpha()
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
      for (particle <- particles if particle.shouldRender && particle.ignoreDepth && particle.isAdditive) {
        particle.renderParticleGlow(buffer, player, partialTicks, x, xz, z, yz, xy)
      }
      tess.draw()

      GlStateManager.enableDepth()
      GlStateManager.enableCull()
      GlStateManager.depthMask(true)
      GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
      GlStateManager.disableBlend()
      GlStateManager.alphaFunc(516, 0.1F)
    }
  }

  def addParticle(particle: IMirrorParticle): Unit = {
    require(particle != null, "Null particle")
    particles += particle
  }
}
