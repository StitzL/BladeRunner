package org.stitz.scala.bladerunner.app

import java.nio.file.Path
import scalafx.beans.property.StringProperty
import scalafx.beans.property.IntegerProperty
import scalafx.beans.property.LongProperty
import scalafx.beans.property.FloatProperty
import scalafx.collections.ObservableSet
import scalafx.collections.ObservableHashSet
import scalafx.beans.Observable
import scalafx.application.Platform
import org.stitz.scala.bladerunner.file.FileResult

case class FileResultBean(path: StringProperty, size: LongProperty, lastModified: StringProperty, hash: StringProperty, percentDone: FloatProperty) {
  def this(_path: String, _size: Long = 0, _lastModified: String = "", _hash: String = "", _percentDone: Float = 0) = this(StringProperty(_path), LongProperty(_size), StringProperty(_lastModified), StringProperty(_hash), FloatProperty(_percentDone))
  
  override def equals(other: Any) = other match {
    case that: FileResultBean =>
      that.path.value == this.path.value
    case _ => false
  }
}