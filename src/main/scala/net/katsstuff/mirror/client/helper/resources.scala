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

import java.util

import javax.annotation.Nullable

import com.google.common.collect.ImmutableMap

import net.minecraft.client.renderer.block.model.{ModelResourceLocation => MRL}
import net.minecraft.util.{IStringSerializable, ResourceLocation}

trait Location {
  def rawPath: String
  def path: String = s"$rawPath/"
}
case class MiscLocation(rawPath: String) extends Location

case class ModelLocation(rawPath: String) extends Location
object ModelLocation {
  val Block = ModelLocation("block")
  val Item  = ModelLocation("item")
  val Obj   = ModelLocation("obj")
}

case class TextureLocation(rawPath: String) extends Location
object TextureLocation {
  val Blocks = TextureLocation("blocks")
  val Items  = TextureLocation("items")
  val Effect = TextureLocation("effect")
  val Entity = TextureLocation("effect")
  val Gui    = TextureLocation("gui")
  val Model  = TextureLocation("model")
}

case class AssetLocation(rawPath: String) extends Location
object AssetLocation {
  val Models   = AssetLocation("models")
  val Shaders  = AssetLocation("shaders")
  val Sounds   = AssetLocation("sounds")
  val Textures = AssetLocation("textures")
}

case class ShaderLocation(rawPath: String) extends Location
case object ShaderLocation {
  val Post    = ShaderLocation("post")
  val Program = ShaderLocation("program")
}

trait ResourceHelperJ {

  def getLocation(
      modId: String,
      @Nullable asset: AssetLocation,
      @Nullable location: Location,
      name: String,
      suffix: String
  ): ResourceLocation = {
    val builder = new StringBuilder
    if (asset != null) builder.append(asset.path)
    if (location != null) builder.append(location.path)
    builder.append(name).append(suffix)
    new ResourceLocation(modId, builder.toString)
  }

  def getModel(modId: String, name: String, variant: String): MRL = {
    val atlas = getAtlas(modId, null, name)
    new MRL(atlas, variant)
  }

  def getAtlas(modId: String, @Nullable location: Location, name: String): ResourceLocation =
    getLocation(modId, null, location, name, "")

  def getTexture(modId: String, @Nullable location: Location, name: String): ResourceLocation =
    getLocation(modId, AssetLocation.Textures, location, name, ".png")

  def getSimple(modId: String, name: String): ResourceLocation = getLocation(modId, null, null, name, "")

  def from(
      amount: Int,
      name: String,
      function: util.function.Function[String, ResourceLocation]
  ): Array[ResourceLocation] = Array.tabulate[ResourceLocation](amount)(i => function(s"$name$i"))

  def from[T <: Enum[T] with IStringSerializable](
      clazz: Class[T],
      name: String,
      function: util.function.Function[String, ResourceLocation]
  ): ImmutableMap[T, ResourceLocation] = {
    val builder = ImmutableMap.builder[T, ResourceLocation]
    val enums   = clazz.getEnumConstants

    enums.foreach(enum => builder.put(enum, function.apply(s"$name${enum.getName}")))

    builder.build()
  }
}
object ResourceHelperJ extends ResourceHelperJ

trait ResourceHelperS {

  def getLocation(
      modId: String,
      asset: Option[AssetLocation],
      location: Option[Location],
      name: String,
      suffix: String
  ): ResourceLocation = {
    val builder = new StringBuilder
    asset.foreach(loc => builder.append(loc.path))
    location.foreach(loc => builder.append(loc.path))
    builder.append(name).append(suffix)
    new ResourceLocation(modId, builder.toString)
  }

  def getModel(modId: String, name: String, variant: String): MRL = {
    val atlas = getAtlas(modId, null, name)
    new MRL(atlas, variant)
  }

  def getAtlas(modId: String, location: Option[Location], name: String): ResourceLocation =
    getLocation(modId, None, location, name, "")

  def getTexture(modId: String, location: Option[Location], name: String): ResourceLocation =
    getLocation(modId, Some(AssetLocation.Textures), location, name, ".png")

  def getSimple(modId: String, name: String): ResourceLocation = getLocation(modId, None, None, name, "")

  def from(amount: Int, name: String, f: String => ResourceLocation): Seq[ResourceLocation] =
    Seq.tabulate[ResourceLocation](amount)(i => f(s"$name$i"))

  def from[T <: Enum[T] with IStringSerializable](
      clazz: Class[T],
      name: String,
      f: String => ResourceLocation
  ): Map[T, ResourceLocation] =
    clazz.getEnumConstants.map(enum => enum -> f(s"$name${enum.getName}")).toMap
}
object ResourceHelperS extends ResourceHelperS
