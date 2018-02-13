/*
 * This file is part of Mirror, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 TeamNightclipse
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
package net.katsstuff.mirror.network.scalachannel

import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.{INetHandler, NetHandlerPlayServer}
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

sealed trait MessageHandler[-A, Reply] {

  def handle(netHandler: INetHandler, a: A): Option[Reply]

  def side: Side
  def scheduler: IThreadListener
}

trait ClientMessageHandler[A, Reply] extends MessageHandler[A, Reply] with HasClientHandler[A] {
  @SideOnly(Side.CLIENT)
  def handle(netHandler: NetHandlerPlayClient, a: A): Option[Reply]
  override def handle(netHandler: INetHandler, a: A): Option[Reply] = handle(netHandler.asInstanceOf[NetHandlerPlayClient], a)
  override def side: Side = Side.CLIENT
  override def scheduler: IThreadListener = Minecraft.getMinecraft
}

trait ServerMessageHandler[A, Reply] extends MessageHandler[A, Reply] with HasServerHandler[A] {
  def handle(netHandler: NetHandlerPlayServer, a: A): Option[Reply]
  override def handle(netHandler: INetHandler, a: A): Option[Reply] = handle(netHandler.asInstanceOf[NetHandlerPlayServer], a)
  override def side: Side = Side.SERVER
  override def scheduler: IThreadListener = FMLCommonHandler.instance().getMinecraftServerInstance
}