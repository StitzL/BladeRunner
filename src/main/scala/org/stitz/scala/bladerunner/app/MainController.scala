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

case class MainController(stage: Stage) {
  def startAnalysis(directory: String) = {
    if (!directory.isEmpty()) {
    	val system = ActorSystem("BladeRunner")
      val actor = system.actorOf(Props[Supervisor],
        name = "supervisor")
        
      val result = actor ! StartWork(Paths.get(directory))
    }
  }

  def stopAnalysis = {
    // need supervisor to implement
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