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

sealed trait WorkMessage
case class StartWork(dir: Path) extends WorkMessage
case class AbortWork() extends WorkMessage
case class StartedOn(dir: Path) extends WorkMessage
case class ProgressOn(dir: Path, percentag: Double) extends WorkMessage
case class Finished(dir: Path, nrOfChildren: Int) extends WorkMessage

object Supervisor {
  private var system: ActorSystem = null
  private var resultProcessorPool: ActorRef = null 
  private var dirProcessorPool: ActorRef = null
  private var fileProcessorPool: ActorRef = null
  
  {
    init(ActorSystem("BladeRunner"))
  }

  private def init(_system: ActorSystem) {
    system = _system
    resultProcessorPool =
      system.actorOf(SmallestMailboxPool(20).props(Props[Supervisor]), "resultProcessor")
    dirProcessorPool =
      system.actorOf(SmallestMailboxPool(20).props(Props[DirectoryProcessor]), "dirProcessor")
    fileProcessorPool =
      system.actorOf(SmallestMailboxPool(20).props(Props[FileProcessor]), "fileProcessor")
  }

  def resultProcessor = resultProcessorPool
  def dirProcessor = dirProcessorPool
  def fileProcessor = fileProcessorPool
}

class Supervisor extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case StartWork(dir) => {
      val result = context.actorOf(Props(classOf[ResultListener], self, dir, 1),
        "result")
      Supervisor.dirProcessor ! Process(dir, result)
    }

    case AbortWork() => {
      Supervisor.system.terminate()
      Supervisor.init(ActorSystem("BladeRunner"))
    }

    case DirectoryResult(path, nrOfChildren) => {
      log.info("\n\t%s processed. \t\tContains %s files.".format(path.toAbsolutePath(), nrOfChildren))
      context.system.terminate()
    }

    case FileResult(path, size, hash) => {
      log.info("Size: %s\t\tHash:%s".format(size, hash))
      ResultController.updateItem(path, size, hash)
    }
    
    case ProgressOn(dir, percentage) => {
      
    }

    case StartedOn(dir) => {
      ResultController.addItem(dir)
    }
    
    case Finished(dir, numberOfFiles) => {
      ResultController.updateItem(dir, numberOfFiles, 0)
    }

    case _ => log.info("Unknown message")
  }
}