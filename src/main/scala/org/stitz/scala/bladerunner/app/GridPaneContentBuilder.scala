package org.stitz.scala.bladerunner.app

import scalafx.scene.control.Control
import scala.collection.mutable.MutableList
import scalafx.scene.layout.GridPane
import scalafx.scene.Node
import scalafx.geometry.HPos
import scalafx.scene.layout.Priority
import scalafx.geometry.Insets

class GridPaneContentBuilder {

  val children = new MutableList[Node]()
  
  def add(child: Node): GridPaneContentBuilder = {
    children+=child
    return this
  }
  
  def setConstraints(col: Int, row: Int) : GridPaneContentBuilder = {
    GridPane.setConstraints(children.last, col, row)
    return this
  }
    
  def setHalignment(hp: HPos) : GridPaneContentBuilder = {
    GridPane.setHalignment(children.last, hp)
    return this
  }
  
  def setHgrow(prio: Priority) : GridPaneContentBuilder = {
    GridPane.setHgrow(children.last, prio)
    return this
  }
  
  def setMargin(margin: Insets) : GridPaneContentBuilder = {
    GridPane.setMargin(children.last, margin)
    return this
  }
  
  def build : List[Node] = {
    return children.toList
  }
}