package org.stitz.scala.bladerunner.app

import scala.concurrent.duration.DurationInt
import org.stitz.scala.bladerunner.file.DirectoryActor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import java.nio.file.Paths
import org.stitz.scala.bladerunner.file.Process
import org.stitz.scala.bladerunner.file.DirectoryResult
import scalafx.stage.DirectoryChooser
import scalafx.beans.property.StringProperty
import scalafx.stage.Stage
import org.stitz.scala.bladerunner.file.ResultListener
import akka.actor.ActorRef

class TotalListener(none: ActorRef) extends ResultListener(none, 1) {
  override def handleResult(): Boolean = {
    val result = super.handleResult()

    if (result) {
      log.info("\n\tFiles found: \t\t%s".format(total))
      context.system.terminate()
    }
    return result
  }
}
case class MainController(stage: Stage) {
  def startAnalysis(directory: String) = {
    if (!directory.isEmpty()) {
    	val system = ActorSystem("BladeRunner")
      val result = system.actorOf(Props(classOf[TotalListener], system.deadLetters),
        name = "result")
      val actor = system.actorOf(Props(classOf[DirectoryActor], result),
        name = "root")
      implicit val timeout = Timeout(25 seconds)
      import system.dispatcher
      actor ! Process(Paths.get(directory))
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