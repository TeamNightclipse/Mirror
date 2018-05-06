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
package net.katsstuff.mirror.network.scalachannel

import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.{INetHandler, NetHandlerPlayServer}
import net.minecraft.util.IThreadListener
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * A type class for handling packets.
  * @tparam A The message to handle.
  * @tparam Reply The reply type.
  */
sealed trait MessageHandler[-A, Reply] {

  /**
    * Handles the packet.
    * @param netHandler The nethandler for this side.
    * @param a The packet instance.
    * @return The reply, if applicable.
    */
  def handle(netHandler: INetHandler, a: A): Option[Reply]

  /**
    * An instance of the [[Side]] object for this side.
    */
  def side: Side

  /**
    * The scheduler used by this side.
    */
  def scheduler: IThreadListener
}

/**
  * A typeclass for handling client packets.
  * @tparam A The message to handle.
  * @tparam Reply The reply type.
  */
trait ClientMessageHandler[A, Reply] extends MessageHandler[A, Reply] with HasClientHandler[A] {

  /**
    * Handles the packet on the client side.
    * @param netHandler The client side nethandler.
    * @param a The packet instance.
    * @return The reply, if applicable.
    */
  @SideOnly(Side.CLIENT)
  def handle(netHandler: NetHandlerPlayClient, a: A): Option[Reply]
  override def handle(netHandler: INetHandler, a: A): Option[Reply] =
    handle(netHandler.asInstanceOf[NetHandlerPlayClient], a)
  override def side: Side                 = Side.CLIENT
  override def scheduler: IThreadListener = Minecraft.getMinecraft
}

/**
  * A typeclass for handling server packets.
  * @tparam A The message to handle.
  * @tparam Reply The reply type.
  */
trait ServerMessageHandler[A, Reply] extends MessageHandler[A, Reply] with HasServerHandler[A] {

  /**
    * Handles the packet on the server side.
    * @param netHandler The server side nethandler.
    * @param a The packet instance.
    * @return The reply, if applicable.
    */
  def handle(netHandler: NetHandlerPlayServer, a: A): Option[Reply]
  override def handle(netHandler: INetHandler, a: A): Option[Reply] =
    handle(netHandler.asInstanceOf[NetHandlerPlayServer], a)
  override def side: Side                 = Side.SERVER
  override def scheduler: IThreadListener = FMLCommonHandler.instance().getMinecraftServerInstance
}
