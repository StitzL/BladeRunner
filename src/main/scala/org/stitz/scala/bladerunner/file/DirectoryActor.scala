package org.stitz.scala.bladerunner.file

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.function.Consumer
import java.util.stream.Stream

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging

sealed trait DirectoryMessage
case class Process(dir: Path) extends DirectoryMessage
 
class DirectoryActor(val listener: ActorRef) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case Process(dir) => {
      log.info(" Processing " + dir)
      if (Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
        var files: Stream[Path] = Files.list(dir)
        try {
          val nrOfFiles = files.count
          files.close();

          files = Files.list(dir)
          log.info(" Directory, files: " + nrOfFiles)
          var i = 0
          val resultListener = context.actorOf(Props(classOf[ResultListener], listener, nrOfFiles), self.path.name + "-r")
          files.forEach(new Consumer[Path]() {
            def accept(path: Path) = {
              i += 1
              val dirRouter = context.actorOf(Props(classOf[DirectoryActor], resultListener), path.getFileName().toString())
              dirRouter ! Process(path)
            }
          })
        } finally {
          files.close();

        }

      } else {
        listener ! FileResult(dir.toFile().length())
        context.stop(self)
      }
    }
    case _ => log.info("Unknown message")
  }

}

