package org.stitz.scala.bladerunner.file

import akka.actor.Actor
import akka.actor.ActorRef
import akka.event.Logging
import java.util.concurrent.atomic.AtomicLong
import java.nio.file.Path
import scala.actors.threadpool.AtomicInteger

sealed trait ResultMessage
case class FileResult(path: Path, size: Int, hash: Int) extends ResultMessage
case class DirectoryResult(path: Path, nrOfChildren: Int) extends ResultMessage 

class ResultListener(parent: ActorRef, path: Path, nrOfFiles: Int) extends Actor {
  val log = Logging(context.system, this)
  private var nrOfResults, totalNrOfChildren: AtomicInteger = new AtomicInteger(0)

  def receive = {
    case msg: FileResult => {

      Supervisor.resultProcessor forward msg
      handleResult()
    }

    case DirectoryResult(path, nrOfChildren) => {
      totalNrOfChildren.addAndGet(nrOfChildren)
      log.info(" Children + " + nrOfChildren)
      Supervisor.resultProcessor ! Finished(path, nrOfChildren)
      handleResult()
    }
    case _ => log.info("Unknown message")
  }
  
  def handleResult() : Boolean = {
    val done = nrOfResults.incrementAndGet() >= nrOfFiles
    if (done) {
      parent ! DirectoryResult(path, totalNrOfChildren.addAndGet(nrOfFiles))
    }
    return done
  }
}