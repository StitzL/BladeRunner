package org.stitz.scala.bladerunner.app

import java.nio.file.Path
import scala.collection.concurrent.TrieMap
import javafx.scene.control.TreeTableView
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty.sfxObjectProperty2jfx
import scalafx.scene.control.TreeItem
import scalafx.scene.control.TreeItem.sfxTreeItemToJfx
import java.net.URI

object ResultController {
  var model = new TrieMap[URI, TreeItem[FileResultBean]]()

  private def createItem(path: Path): TreeItem[FileResultBean] = {
    val treeItem = new TreeItem(new FileResultBean(path.getFileName().toString()))
    treeItem.setExpanded(true)
    model.put(path.toAbsolutePath().toUri(), treeItem)

    return treeItem
  }

  def setRoot(path: Path): TreeItem[FileResultBean] = {
    return createItem(path)
  }

  def addItem(path: Path) = {
    val foundItem = model.get(path.toAbsolutePath().toUri())
    if (foundItem.isEmpty) {
      val treeItem = createItem(path)

      Platform.runLater {
        val parentItem = model.get(path.getParent().toAbsolutePath().toUri())
        if (parentItem.isDefined) {
          parentItem.get.children += treeItem
        }
      }
    }
  }

  def updateItem(path: Path, size: Int, hash: Int) {
    val treeItem = model.get(path.toAbsolutePath().toUri());
    if (treeItem.isDefined) {
      val bean = treeItem.get.value.get
      val sizeProperty = bean.size
      val hashProperty = bean.hash
      val hashString = if (hash == 0) "---" else hash.toHexString
      Platform.runLater { sizeProperty.update(size); hashProperty.update(hashString) }
    }
  }

}