package org.stitz.scala.bladerunner.file

import akka.actor.Actor
import akka.actor.ActorRef
import akka.event.Logging
import java.util.concurrent.atomic.AtomicLong

sealed trait ResultMessage
case class FileResult(size: Long, hash: Int) extends ResultMessage
case class DirectoryResult(nrOfChildren: Long) extends ResultMessage 

class ResultListener(parent: ActorRef, supervisor: ActorRef, nrOfFiles: Long) extends Actor {
  val log = Logging(context.system, this)
  private var nrOfResults, totalNrOfChildren: AtomicLong = new AtomicLong(0)

  def receive = {
    case msg: FileResult => {

      supervisor forward msg
      handleResult()
    }

    case DirectoryResult(nrOfChildren) => {
      totalNrOfChildren.addAndGet(nrOfChildren)
      log.info(" Children + " + nrOfChildren)
      handleResult()
    }
    case _ => log.info("Unknown message")
  }
  
  def handleResult() : Boolean = {
    val done = nrOfResults.incrementAndGet() >= nrOfFiles
    if (done) {
      parent ! DirectoryResult(totalNrOfChildren.addAndGet(nrOfFiles))
      log.info(" Done - stopping")
      context.stop(self)
    }
    return done
  }
  
  def getSupervisor = supervisor
}