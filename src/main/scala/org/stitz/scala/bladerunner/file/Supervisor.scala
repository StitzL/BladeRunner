package org.stitz.scala.bladerunner.file

import java.nio.file.Path
import org.stitz.scala.bladerunner.app.ResultController
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.Logging
import akka.routing.SmallestMailboxPool
import scalafx.beans.property.BooleanProperty
import akka.routing.Broadcast
import akka.actor.PoisonPill

sealed trait WorkMessage
case class StartedOn(dir: Path) extends WorkMessage
case class ProgressOn(dir: Path, processed: Long, filesToProcess: Long) extends WorkMessage

object Supervisor {
  val isRunning = BooleanProperty(isRunning_)
  private var system: ActorSystem = null
  private var resultProcessorPool: ActorRef = null
  private var dirProcessorPool: ActorRef = null
  private var fileProcessorPool: ActorRef = null

  def init(_system: ActorSystem) {
    abort
    system = _system
    resultProcessorPool =
      system.actorOf(SmallestMailboxPool(20).props(Props[Supervisor]), "resultProcessor")
    dirProcessorPool =
      system.actorOf(SmallestMailboxPool(20).props(Props[DirectoryProcessor]), "dirProcessor")
    fileProcessorPool =
      system.actorOf(SmallestMailboxPool(20).props(Props[FileProcessor]), "fileProcessor")
    isRunning.set(isRunning_)
  }

  def resultProcessor = resultProcessorPool
  def dirProcessor = dirProcessorPool
  def fileProcessor = fileProcessorPool

  private def isRunning_ = system != null
  def abort = {
    if (isRunning_) {
      dirProcessor ! Broadcast(PoisonPill)
      fileProcessor ! Broadcast(PoisonPill)
      resultProcessor ! Broadcast(PoisonPill)
      FileDigester.system.terminate()
      system.terminate()
      system = null;
      isRunning.set(isRunning_)
    }
  }
  def start(dir: Path) = dirProcessor ! Process(dir, resultProcessorPool)

}

class Supervisor extends Actor {
  val log = Logging(context.system, this)

  def receive = {

    case DirectoryResult(path, lastModified, nrOfChildren) => {
      log.info("\n\t%s processed. \t\tContains %s files.".format(path.toAbsolutePath(), nrOfChildren))
      Supervisor.abort
    }

    case msg: FileResult => {
      ResultController.updateItem(msg)
    }

    case ProgressOn(dir, processed, toProcess) => {
      ResultController.recordProgress(dir, processed, toProcess)
    }

    case StartedOn(dir) => {
      ResultController.addItem(dir)
    }

    case _ => log.info("Unknown message")
  }
}