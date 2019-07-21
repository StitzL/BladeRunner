package org.stitz.scala.bladerunner.app

import java.net.URI
import java.nio.file.Path
import java.time.format.DateTimeFormatter
import java.time.{Instant, OffsetDateTime, ZoneId}
import java.util.concurrent.Executor
import java.util.{Base64, Locale}

import org.stitz.scala.bladerunner.file.FileResult
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty.sfxObjectProperty2jfx
import scalafx.scene.control.TreeItem
import scalafx.scene.control.TreeItem.sfxTreeItemToJfx

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.HashSet
import scala.concurrent.ExecutionContext

object ScalaFXExecutionContext {
  implicit val scalaFxExecutionContext: ExecutionContext = ExecutionContext.fromExecutor(new Executor {
    def execute(command: Runnable): Unit = Platform.runLater(command)
  })
}
object ResultController {
  val treeModel = new TrieMap[URI, TreeItem[FileResultBean]]()
  val similarFiles = new TrieMap[String, HashSet[URI]]()

  private def createItem(path: Path): TreeItem[FileResultBean] = {
    val treeItem = new TreeItem(new FileResultBean(path.getFileName().toString()))
    treeItem.setExpanded(true)
    treeModel.put(id(path), treeItem)

    return treeItem
  }

  def setRoot(path: Path): TreeItem[FileResultBean] = {
    treeModel.clear()
    return createItem(path)
  }

  def addItem(path: Path) = {
      val parent = treeModel.getOrElseUpdate(id(path.getParent()), { createItem(path.getParent()) })
      Platform.runLater {
        parent.children += treeModel.getOrElseUpdate(id(path), { createItem(path) })
      }
  }

  def updateItem(msg: FileResult) = {
    import msg._
    val date = OffsetDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault())
    val timestamp = date.format(DateTimeFormatter.ofPattern("dd.MMM.yyyy HH:mm:ss", Locale.getDefault()))
    val hashString = if (hash == null) "None" else Base64.getEncoder().encodeToString(hash.toArray)

    similarFiles.getOrElseUpdate(hashString, {
      HashSet.empty
    }) += (id(path))
    val bean = findBean(path)
    bean.hash.set(hashString)
    bean.size.set(size)
    bean.lastModified.set(timestamp)

  }

  def recordProgress(path: Path, processed: Long, filesToProcess: Long) = {
    Platform.runLater {
      val bean = findBean(path)
      bean.percentDone.set((processed + 1).toFloat / filesToProcess)
      bean.size.set(processed)
    }
  }

  private def id(path: Path) = path.toAbsolutePath().toUri()
  
  val defaultItem = new TreeItem(new FileResultBean("<none>"))
  private def findBean(path: Path): FileResultBean = {
    val result = treeModel.getOrElse(id(path), defaultItem)
    if (result == defaultItem) println("Did not find " + id(path))
    return result.value.get;
  }

}