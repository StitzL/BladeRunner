package org.stitz.scala.bladerunner.file

import java.nio.file.Path

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.Logging

sealed trait WorkMessage
case class StartWork(dir: Path) extends WorkMessage
case class AbortWork(timeout: Int) extends WorkMessage
class Supervisor extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case StartWork(dir) => {
      val result = context.actorOf(Props(classOf[ResultListener], self, self, 1l),
        "result")
      val actor = context.actorOf(Props(classOf[DirectoryProcessor], result, self),
        "root")
      actor ! Process(dir)
    }

    case AbortWork(timeout) => {
      // implement abort
    }

    case DirectoryResult(nrOfChildren) => {
      log.info("\n\tFiles processed: \t\t%s".format(nrOfChildren))
      context.system.terminate()
    }

    case FileResult(size, hash) => {
      log.info("Size: %s\t\tHash:%s".format(size, hash))
    }

    case _ => log.info("Unknown message")
  }
}