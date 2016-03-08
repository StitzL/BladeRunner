package org.stitz.scala.bladerunner.file

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.function.Consumer
import java.util.stream.Stream

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinPool

sealed trait DirectoryMessage
case class Process(dir: Path) extends DirectoryMessage
case class FileResult(size: Long) extends DirectoryMessage
case class DirectoryResult(nrOfChildren: Long) extends DirectoryMessage

class DirectoryActor() extends Actor {
  var parent: ActorRef = context.system.deadLetters
  private var nrOfFiles, nrOfResults, totalNrOfChildren: Long = 0

  def receive = idle
  
  val idle: Receive= {
    case Process(dir) => {
      log(" Processing " + dir)
      if (Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
        parent = sender
        var files : Stream[Path] = Files.list(dir)
        try {
          nrOfFiles = files.count
        } finally {
          files.close();
        }

        files = Files.list(dir)
        try {
          log(" Directory, files: " + nrOfFiles)
          var i = 0
          files.forEach(new Consumer[Path]() {
            def accept(path:Path) = {
              i += 1
              val dirRouter = context.actorOf(Props[DirectoryActor], self.path.name + "-" + i)
              dirRouter ! Process(path) 
            }
          })
        } finally {
          files.close();
        }

        context.become(waitingForResults)
      } else {
        sender ! FileResult(dir.toFile().length())
        context.stop(self)
      }
    }
  }

  val waitingForResults: Receive = {
    case FileResult(size) => {
      log(" Size: " + size)
      handleResult()
    }

    case DirectoryResult(nrOfChildren) => {
      totalNrOfChildren += nrOfChildren
      log(" Children + " + nrOfChildren)
      handleResult()
    }
  }

  private def handleResult() = {
    nrOfResults += 1
    if (nrOfResults == nrOfFiles) {
      parent ! DirectoryResult(nrOfFiles + totalNrOfChildren)
      log(" Done - stopping")
      context.stop(self)
    }
  }

  private def log(msg: String) = {
    println(self.path.name + " - " + msg)
  }
}

