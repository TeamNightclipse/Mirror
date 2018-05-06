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
package net.katsstuff.mirror.client.helper

import net.minecraft.block.Block
import net.minecraft.client.renderer.block.model.{ModelResourceLocation => MRL}
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.util.IStringSerializable
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Various methods to aid in registering in block and item models.
  */
@SideOnly(Side.CLIENT)
object ModelHelper {

  def registerModel(block: Block, meta: Int, location: MRL): Unit = {
    val item = Item.getItemFromBlock(block)
    if (item == Items.AIR) throw new UnsupportedOperationException("This block has no Item!")
    registerModel(item, meta, location)
  }

  def registerModel(item: Item, meta: Int, location: MRL): Unit =
    ModelLoader.setCustomModelResourceLocation(item, meta, location)

  def registerModel(block: Block, meta: Int, variant: String): Unit = {
    val item = Item.getItemFromBlock(block)
    if (item == Items.AIR) throw new UnsupportedOperationException("This block has no Item!")
    registerModel(item, meta, variant)
  }

  def registerModel(item: Item, meta: Int, variant: String): Unit = {
    val location = new MRL(item.getRegistryName, variant)
    ModelLoader.setCustomModelResourceLocation(item, meta, location)
  }

  def registerModel(block: Block, meta: Int): Unit = {
    val item = Item.getItemFromBlock(block)
    if (item == Items.AIR) throw new UnsupportedOperationException("This block has no Item!")
    registerModel(item, meta)
  }

  def registerModel(item: Item, meta: Int): Unit = {
    val location = new MRL(item.getRegistryName, "inventory")
    ModelLoader.setCustomModelResourceLocation(item, meta, location)
  }

  def registerModel[T <: Enum[T] with IStringSerializable](block: Block, clazz: Class[T]): Unit = {
    val item = Item.getItemFromBlock(block)
    if (item == Items.AIR) throw new UnsupportedOperationException("This block has no Item!")
    registerModel(item, clazz)
  }

  def registerModel[T <: Enum[T] with IStringSerializable](item: Item, clazz: Class[T]): Unit =
    for (t <- clazz.getEnumConstants) {
      val mrl = new MRL(s"${item.getRegistryName}_${t.getName}", "inventory")
      ModelLoader.setCustomModelResourceLocation(item, t.ordinal, mrl)
    }
}
