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

/**
  * Represents a location where resources can be found.
  */
trait Location {
  def rawPath: String
  def path: String = s"$rawPath/"
}

/**
  * Represents any arbitrary location.
  */
case class MiscLocation(rawPath: String) extends Location

/**
  * Represents a (block) model location.
  */
case class ModelLocation(rawPath: String) extends Location
object ModelLocation {
  val Block = ModelLocation("block")
  val Item  = ModelLocation("item")
  val Obj   = ModelLocation("obj")
}

/**
  * Represents a texture location.
  */
case class TextureLocation(rawPath: String) extends Location
object TextureLocation {
  val Blocks    = TextureLocation("blocks")
  val Items     = TextureLocation("items")
  val Effect    = TextureLocation("effect")
  val Particles = TextureLocation("particles")
  val Entity    = TextureLocation("effect")
  val Gui       = TextureLocation("gui")
  val Model     = TextureLocation("model")
}

/**
  * Represents a root asset location.
  */
case class AssetLocation(rawPath: String) extends Location
object AssetLocation {
  val Models   = AssetLocation("models")
  val Shaders  = AssetLocation("shaders")
  val Sounds   = AssetLocation("sounds")
  val Textures = AssetLocation("textures")
}

/**
  * Represents a minecraft shader location.
  */
case class ShaderLocation(rawPath: String) extends Location
case object ShaderLocation {
  val Post    = ShaderLocation("post")
  val Program = ShaderLocation("program")
}

trait ResourceHelperJ {

  /**
    * Gets a location based on an asset location and another location, together
    * with a name and a suffix.
    * @param modId The modId for this resource.
    * @param name The name of the resource.
    * @param asset The asset location to get the resource from.
    * @param location The child location to get the resource from.
    * @param suffix The suffix to append at the end.
    */
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

  /**
    * Generates a [[MRL]] from a name and a variant.
    * @param modId The modId for this resource.
    * @param name The name of the resource.
    * @param variant The variant to get.
    */
  def getModel(modId: String, name: String, variant: String): MRL = {
    val atlas = getAtlas(modId, null, name)
    new MRL(atlas, variant)
  }

  /**
    * Gets a resource without an asset location.
    * @param modId The modId for this resource.
    * @param name The name of the resource.
    * @param location The location to get from.
    */
  def getAtlas(modId: String, @Nullable location: Location, name: String): ResourceLocation =
    getLocation(modId, null, location, name, "")

  /**
    * Gets a texture given a specific location.
    * @param modId The modId for this resource.
    * @param name The name of the resource.
    * @param location The location within the textures folder.
    */
  def getTexture(modId: String, @Nullable location: Location, name: String): ResourceLocation =
    getLocation(modId, AssetLocation.Textures, location, name, ".png")

  /**
    * Gets a resource without any locations.
    * @param modId The modId for this resource.
    * @param name The name of the resource.
    */
  def getSimple(modId: String, name: String): ResourceLocation = getLocation(modId, null, null, name, "")

  /**
    * Generates an array from amount and a mapping function.
    * @param amount The amount of cases to generate.
    * @param name The base name to use for all entries.
    * @param function The mapping function.
    */
  def from(
      amount: Int,
      name: String,
      function: util.function.Function[String, ResourceLocation]
  ): Array[ResourceLocation] = Array.tabulate[ResourceLocation](amount)(i => function(s"$name$i"))

  /**
    * Generates map from a java enum and a mapping function.
    * @param clazz The enum class.
    * @param name The base name to use for all entries.
    * @param function The mapping function.
    * @tparam T The enum type.
    */
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
object ResourceHelperStatic extends ResourceHelperJ

trait ResourceHelperS {

  def modId: String

  /**
    * Gets a location based on an asset location and another location, together
    * with a name and a suffix.
    * @param name The name of the resource.
    * @param asset The asset location to get the resource from.
    * @param location The child location to get the resource from.
    * @param suffix The suffix to append at the end.
    */
  def getLocation(
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

  /**
    * Generates a [[MRL]] from a name and a variant.
    * @param name The name of the resource.
    * @param variant The variant to get.
    */
  def getModel(name: String, variant: String): MRL = {
    val atlas = getAtlas(None, name)
    new MRL(atlas, variant)
  }

  /**
    * Gets a resource without an asset location.
    * @param name The name of the resource.
    * @param location The location to get from.
    */
  def getAtlas(location: Option[Location], name: String): ResourceLocation =
    getLocation(None, location, name, "")

  /**
    * Gets a texture given a specific location.
    * @param name The name of the resource.
    * @param location The location within the textures folder.
    */
  def getTexture(location: Option[Location], name: String): ResourceLocation =
    getLocation(Some(AssetLocation.Textures), location, name, ".png")

  /**
    * Gets a resource without any locations.
    * @param name The name of the resource.
    */
  def getSimple(name: String): ResourceLocation = getLocation(None, None, name, "")

  /**
    * Generates a sequence from amount and a mapping function.
    * @param amount The amount of cases to generate.
    * @param name The base name to use for all entries.
    * @param f The mapping function.
    */
  def from(amount: Int, name: String, f: String => ResourceLocation): Seq[ResourceLocation] =
    Seq.tabulate[ResourceLocation](amount)(i => f(s"$name$i"))

  /**
    * Generates map from a java enum and a mapping function.
    * @param clazz The enum class.
    * @param name The base name to use for all entries.
    * @param f The mapping function.
    * @tparam T The enum type.
    */
  def from[T <: Enum[T] with IStringSerializable](
      clazz: Class[T],
      name: String,
      f: String => ResourceLocation
  ): Map[T, ResourceLocation] =
    clazz.getEnumConstants.map(enum => enum -> f(s"$name${enum.getName}")).toMap
}
class ResourceHelperClass(val modId: String) extends ResourceHelperS
