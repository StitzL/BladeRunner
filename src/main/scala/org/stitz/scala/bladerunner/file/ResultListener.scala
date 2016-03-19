package org.stitz.scala.bladerunner.file

import akka.actor.Actor
import akka.actor.ActorRef
import akka.event.Logging
import java.util.concurrent.atomic.AtomicLong
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import akka.util.ByteString
import scala.concurrent.Future

sealed trait ResultMessage
case class FileResult(path: Path, size: Long, lastModified: Long, hash: Future[ByteString]) extends ResultMessage
case class DirectoryResult(path: Path, lastModified: Long, nrOfChildren: Int) extends ResultMessage 

class ResultListener(parent: ActorRef, path: Path, nrOfFiles: Int) extends Actor {
  val log = Logging(context.system, this)
  private var nrOfResults, totalNrOfChildren: AtomicInteger = new AtomicInteger(0)

  def receive = {
    case msg: FileResult => {

      Supervisor.resultProcessor forward msg
      handleResult()
    }

    case DirectoryResult(path, lastModified, nrOfChildren) => {
      totalNrOfChildren.addAndGet(nrOfChildren)
      Supervisor.resultProcessor ! FileResult(path, nrOfChildren, lastModified, null)
      handleResult()
    }
    case _ => log.info("Unknown message")
  }
  
  def handleResult() : Boolean = {
    val alreadyProcessed = nrOfResults.incrementAndGet()
    val done = alreadyProcessed >= nrOfFiles
    if (done) {
      parent ! DirectoryResult(path, path.toFile().lastModified(), totalNrOfChildren.addAndGet(nrOfFiles))
    } else {
      Supervisor.resultProcessor ! ProgressOn(path, alreadyProcessed, nrOfFiles)
    }
    return done
  }
}