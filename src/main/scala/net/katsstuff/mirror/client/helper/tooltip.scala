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

import java.text.NumberFormat
import java.util
import java.util.function.BooleanSupplier

import scala.annotation.varargs
import scala.collection.JavaConverters._
import scala.language.implicitConversions

import net.katsstuff.mirror.scalastuff.MirrorImplicits._
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.util.text.TextFormatting

/**
  * Represents a helper to create tooltips.
  * @param objs The currently not queued objects.
  * @param lines The finished lines.
  */
case class Tooltip(objs: Seq[String], lines: Seq[String]) {

  def numberFormat: NumberFormat = {
    val locale = Minecraft.getMinecraft.getLanguageManager.getCurrentLanguage.getJavaLocale
    NumberFormat.getNumberInstance(locale)
  }

  /**
    * Starts a condition.
    * @param condition The condition.
    * @return A object to continue building the condition case.
    */
  def when(condition: => Boolean): Condition = Condition(() => condition, this)

  /**
    * Starts a condition.
    * @param condition The condition.
    * @return A object to continue building the condition case.
    */
  def condition(condition: BooleanSupplier): Condition = Condition(condition.asScala, this)

  /**
    * Starts a condition.
    * @param condition The condition.
    * @return A object to continue building the condition case.
    */
  def condition(condition: KeyCondition): Condition = condition(this)

  /**
    * Adds a object to the current line, followed by a bunch of formatting.
    * @param obj The object to add.
    * @param formats The formatting to use.
    * @return A tooltip builder with the object added.
    */
  @varargs def add(obj: AnyRef, formats: TextFormatting*): Tooltip =
    Tooltip(objs ++ formats.map(_.toString) :+ String.valueOf(obj), lines)

  /**
    * Adds a number to the current line and formats it according to the current language.
    * @param num The number to add.
    * @param formats The formatting to use.
    * @return A tooltip builder with the number added.
    */
  def addNum(num: Double, formats: TextFormatting*): Tooltip =
    add(numberFormat.format(num), formats: _*)

  /**
    * Adds a localized string to the current line, followed by a bunch of formatting.
    * @param i18n The localization key for the string to add.
    * @param formats The formatting to use.
    * @return A tooltip builder with the object added.
    */
  @varargs def addI18n(i18n: String, formats: TextFormatting*): Tooltip =
    add(I18n.format(i18n), formats: _*)

  /**
    * Adds a space on the current line.
    */
  def space: Tooltip = add(" ")

  /**
    * Finishes the current line and adds that line to the finished lines.
    */
  def newline: Tooltip = Tooltip(Seq.empty, lines :+ objs.mkString)

  /**
    * Adds a new empty line to the lines.
    */
  def skipLine: Tooltip = Tooltip(objs, lines :+ "")

  /**
    * Adds all the lines currently queued to a tooltip builder.
    */
  def build(tooltip: util.List[String]): Unit = tooltip.addAll(lines.asJava)
}

object Tooltip extends Tooltip(Seq.empty, Seq.empty) {

  val DarkGrayItalic = Seq(TextFormatting.DARK_GRAY, TextFormatting.ITALIC)
  val GrayItalic     = Seq(TextFormatting.GRAY, TextFormatting.ITALIC)

  def inline: Tooltip = this
}

/**
  * A condition which is valid when a key is pressed.
  */
sealed trait KeyCondition {

  /**
    * Tests this condition.
    */
  def condition: Boolean

  /**
    * The text to show is the condition is not valid.
    */
  def or(tooltip: Tooltip): Tooltip

  /**
    * Adds this condition to the tooltip passed in.
    */
  def apply(tooltip: Tooltip): Condition = Condition(() => condition, tooltip).orElse(or)
}
object KeyCondition {

  /**
    * A condition which is always true.
    */
  case object Nothing extends KeyCondition {
    override def condition: Boolean            = true
    override def or(tooltip: Tooltip): Tooltip = tooltip
  }

  def nothing: Nothing.type = Nothing

  /**
    * A condition which is valid when the shift key is pressed down.
    */
  case object ShiftKeyDown extends KeyCondition {
    override def condition: Boolean = GuiScreen.isShiftKeyDown
    override def or(tooltip: Tooltip): Tooltip =
      tooltip.addI18n("mirror.tooltip.shiftForInfo", Tooltip.DarkGrayItalic: _*).newline
  }

  /**
    * A condition which is valid when the shift key is pressed down.
    */
  def shiftKeyDown: ShiftKeyDown.type = ShiftKeyDown

  /**
    * A condition which is valid when the control key is pressed down.
    */
  case object ControlKeyDown extends KeyCondition {
    override def condition: Boolean = GuiScreen.isCtrlKeyDown
    override def or(tooltip: Tooltip): Tooltip =
      tooltip.addI18n("mirror.tooltip.ctrlForInfo", Tooltip.DarkGrayItalic: _*).newline
  }

  /**
    * A condition which is valid when the control key is pressed down.
    */
  def controlKeyDown: ControlKeyDown.type = ControlKeyDown

  /**
    * A condition which is valid when the alt key is pressed down.
    */
  case object AltKeyDown extends KeyCondition {
    override def condition: Boolean = GuiScreen.isAltKeyDown
    override def or(tooltip: Tooltip): Tooltip =
      tooltip.addI18n("mirror.tooltip.altForInfo", Tooltip.DarkGrayItalic: _*).newline
  }

  /**
    * A condition which is valid when the alt key is pressed down.
    */
  def altKeyDown: AltKeyDown.type = AltKeyDown
}

/**
  * A condition for displaying some text.
  * @param condition The boolean condition.
  * @param tooltip The base tooltip.
  * @param present The action to do if the condition is valid.
  * @param or The action to do if the condition is not valid.
  */
case class Condition(
    condition: () => Boolean,
    tooltip: Tooltip,
    present: Tooltip => Tooltip = identity,
    or: Tooltip => Tooltip = identity
) {

  /**
    * Sets what to render if this condition is true.
    */
  def ifTrue(present: Tooltip => Tooltip): Condition = copy(present = present)

  /**
    * Sets what to render if this condition is true.
    */
  def ifTrueJ(present: util.function.Function[Tooltip, Tooltip]): Condition = copy(present = present.asScala)

  /**
    * Sets what to render if this condition is not true.
    */
  def orElse(or: Tooltip => Tooltip): Condition = copy(or = or)

  /**
    * Sets what to render if this condition is not true.
    */
  def orElseJ(or: util.function.Function[Tooltip, Tooltip]): Condition = copy(or = or.asScala)

  /**
    * Applies this condition to the tooltip.
    */
  def apply: Tooltip = if (condition()) present(tooltip) else or(tooltip)
}
object Condition {
  implicit def implicitApply(condition: Condition): Tooltip = condition.apply
}
