package org.stitz.scala.bladerunner.app

import java.nio.file.Path
import java.nio.file.Paths

import scala.concurrent.duration.DurationInt

import org.stitz.scala.bladerunner.file.Supervisor

import akka.actor.ActorSystem
import akka.pattern.ask
import javafx.scene.control.TreeTableView
import scalafx.beans.property.StringProperty
import scalafx.scene.control.TreeItem
import scalafx.stage.DirectoryChooser
import scalafx.stage.Stage

case class MainController(stage: Stage) {
  def startAnalysis(directory: String, resultView: TreeTableView[FileResultBean]) = {
    if (!directory.isEmpty()) {
      val path = Paths.get(directory)
      resultView.setRoot(ResultController.setRoot(path));
    	Supervisor.init(ActorSystem("BladeRunner"))
      Supervisor.start(path)
    }
  }

  def stopAnalysis = {
    Supervisor.abort 
  }

  def selectFile(result: StringProperty) = {
    val fileChooser = new DirectoryChooser() {
      title = "Pick a directory to analyse"
    }
    
    val preset = result.value 
    if (! (preset == null || preset.trim().isEmpty())) {
      fileChooser.initialDirectory = Paths.get(preset).toFile()
    }

    val selected = fileChooser.showDialog(stage)
    if (selected != null) {
      result.update(selected.getAbsolutePath)
    }
  }
}