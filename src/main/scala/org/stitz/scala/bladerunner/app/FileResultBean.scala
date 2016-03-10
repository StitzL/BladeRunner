package org.stitz.scala.bladerunner.app

import java.nio.file.Path
import scalafx.beans.property.StringProperty
import scalafx.beans.property.IntegerProperty

case class FileResultBean(path: StringProperty, size: IntegerProperty, hash: StringProperty) {
  def this(_path: String, _size: Int = 0, _hash: String = "") = this(StringProperty(_path), IntegerProperty(_size), StringProperty(_hash))
  
  override def equals(other: Any) = other match {
    case that: FileResultBean =>
      that.path.value == this.path.value
    case _ => false
  }
}