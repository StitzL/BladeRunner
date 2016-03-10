package org.stitz.scala.bladerunner.app

import java.nio.file.Paths
import scala.concurrent.duration.DurationInt
import org.stitz.scala.bladerunner.file.StartWork
import org.stitz.scala.bladerunner.file.Supervisor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scalafx.beans.property.StringProperty
import scalafx.stage.DirectoryChooser
import scalafx.stage.Stage
import javafx.scene.control.TreeTableView
import scalafx.scene.control.TreeItem
import scala.collection.parallel.mutable.ParHashMap
import java.nio.file.Path
import org.stitz.scala.bladerunner.file.AbortWork

case class MainController(stage: Stage) {
  def startAnalysis(directory: String, resultView: TreeTableView[FileResultBean]) = {
    if (!directory.isEmpty()) {
      val path = Paths.get(directory)
      resultView.setRoot(ResultController.setRoot(path));

      val result = Supervisor.resultProcessor ! StartWork(path)
    }
  }

  def stopAnalysis = {
    Supervisor.resultProcessor ! AbortWork 
  }

  def selectFile(result: StringProperty) = {
    val fileChooser = new DirectoryChooser() {
      title = "Pick a directory to analyse"
    }

    val selected = fileChooser.showDialog(stage)
    if (selected != null) {
      result.update(selected.getAbsolutePath)
    }
  }
}