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
package net.katsstuff.mirror.helper

import org.apache.logging.log4j.{Level, LogManager, Logger}

import net.katsstuff.mirror.Mirror

private[mirror] object MirrorLogHelper {
  //We use a separate logger until we receive one from Forge
  private var log = LogManager.getLogger(Mirror.Id)

  private var setLogger = false

  def setLog(log: Logger): Unit = {
    if (setLogger) throw new IllegalStateException("Log has already been set")
    setLogger = true
    this.log = log
  }

  private def log(logLevel: Level, obj: Any): Unit = log.log(logLevel, obj)

  private def log(logLevel: Level, e: Throwable, obj: Any): Unit = log.log(logLevel, obj, e)

  def debug(obj: Any): Unit = log(Level.DEBUG, obj)
  def error(obj: Any): Unit = log(Level.ERROR, obj)
  def fatal(obj: Any): Unit = log(Level.FATAL, obj)
  def info(obj: Any):  Unit = log(Level.INFO, obj)
  def trace(obj: Any): Unit = log(Level.TRACE, obj)
  def warn(obj: Any):  Unit = log(Level.WARN, obj)

  def error(obj: Any, e: Throwable): Unit = log(Level.ERROR, e, obj)
  def warn(obj: Any, e: Throwable):  Unit = log(Level.WARN, e, obj)
}
