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
package net.katsstuff.teamnightclipse.mirror

import net.katsstuff.teamnightclipse.mirror.helper.MirrorLogHelper
import net.katsstuff.teamnightclipse.mirror.network.{MirrorPacketHandler, Vector3Serializer}
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.{Mod, SidedProxy}

@Mod(modid = Mirror.Id, name = Mirror.Name, version = Mirror.Version, modLanguage = "scala")
object Mirror {
  final val Id          = "mirror"
  final val Name        = "Mirror"
  final val Version     = "0.4"
  final val CommonProxy = "net.katsstuff.teamnightclipse.mirror.CommonProxy"
  final val ClientProxy = "net.katsstuff.teamnightclipse.mirror.client.ClientProxy"

  def resource(path: String): ResourceLocation = new ResourceLocation(Id, path)

  //JAVA-API
  def instance: Mirror.type = this

  @SidedProxy(clientSide = ClientProxy, serverSide = CommonProxy, modId = Id)
  var proxy: CommonProxy = _

  @Mod.EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    MirrorLogHelper.setLog(event.getModLog)
    MirrorPacketHandler.load()
    DataSerializers.registerSerializer(Vector3Serializer)
    proxy.registerRenderers()
  }

  @Mod.EventHandler
  def init(event: FMLInitializationEvent): Unit =
    proxy.bakeRenderModels()
}
