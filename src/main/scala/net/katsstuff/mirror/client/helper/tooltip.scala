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
import java.util.function.BooleanSupplier

import scala.annotation.varargs
import scala.collection.JavaConverters._
import scala.language.implicitConversions

import net.katsstuff.mirror.scalastuff.MirrorImplicits._
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.util.text.TextFormatting

case class Tooltip(objs: Seq[String], lines: Seq[String]) {

  def when(condition: => Boolean):           Condition = Condition(() => condition, this)
  def condition(condition: BooleanSupplier): Condition = Condition(condition.asScala, this)

  def condition(condition: KeyCondition): Condition = condition(this)

  @varargs def add(obj: AnyRef, formats: TextFormatting*): Tooltip =
    Tooltip(objs ++ formats.map(_.toString) :+ String.valueOf(obj), lines)

  @varargs def addI18n(i18n: String, formats: TextFormatting*): Tooltip =
    add(I18n.format(i18n), formats: _*)

  def newline: Tooltip = Tooltip(Seq.empty, lines :+ objs.mkString)

  def skipLine: Tooltip = Tooltip(objs, lines :+ "")

  def build(tooltip: util.List[String]): Unit = tooltip.addAll(lines.asJava)
}

object Tooltip extends Tooltip(Seq.empty, Seq.empty) {

  val DarkGrayItalic = Seq(TextFormatting.DARK_GRAY, TextFormatting.ITALIC)
  val GrayItalic     = Seq(TextFormatting.GRAY, TextFormatting.ITALIC)

  def inline: Tooltip = this
}

sealed trait KeyCondition {
  def condition:            Boolean
  def or(tooltip: Tooltip): Tooltip

  def apply(tooltip: Tooltip): Condition = Condition(() => condition, tooltip).orElse(or _)
}
object KeyCondition {
  case object Nothing extends KeyCondition {
    override def condition:            Boolean = true
    override def or(tooltip: Tooltip): Tooltip = tooltip
  }

  case object ShiftKeyDown extends KeyCondition {
    override def condition: Boolean = GuiScreen.isShiftKeyDown
    override def or(tooltip: Tooltip): Tooltip =
      tooltip.addI18n("mirror.tooltip.shiftForInfo", Tooltip.DarkGrayItalic: _*).newline
  }

  case object ControlKeyDown extends KeyCondition {
    override def condition: Boolean = GuiScreen.isCtrlKeyDown
    override def or(tooltip: Tooltip): Tooltip =
      tooltip.addI18n("mirror.tooltip.ctrlForInfo", Tooltip.DarkGrayItalic: _*).newline
  }

  case object AltKeyDown extends KeyCondition {
    override def condition: Boolean = GuiScreen.isAltKeyDown
    override def or(tooltip: Tooltip): Tooltip =
      tooltip.addI18n("mirror.tooltip.altForInfo", Tooltip.DarkGrayItalic: _*).newline
  }
}

case class Condition(
    condition: () => Boolean,
    tooltip: Tooltip,
    present: Tooltip => Tooltip = identity,
    or: Tooltip => Tooltip = identity
) {
  def ifTrue(present: Tooltip => Tooltip):                       Condition = copy(present = present)
  def ifTrue(present: util.function.Function[Tooltip, Tooltip]): Condition = copy(present = present.asScala)

  def orElse(or: Tooltip => Tooltip):                       Condition = copy(or = or)
  def orElse(or: util.function.Function[Tooltip, Tooltip]): Condition = copy(or = or.asScala)

  def apply: Tooltip = if (condition()) present(tooltip) else or(tooltip)
}
object Condition {
  implicit def implicitApply(condition: Condition): Tooltip = condition.apply
}
