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
import akka.stream.scaladsl.FileIO
import scala.util.hashing.MurmurHash3

sealed trait DirectoryMessage
case class Process(path: Path) extends DirectoryMessage
 
class FileProcessor(listener: ActorRef, supervisor: ActorRef) extends Actor {
  val log = Logging(context.system, this)
  def receive = {
    case Process(path) => {
      val bytes = Files.readAllBytes(path)
      val hash = MurmurHash3.bytesHash(bytes)
      listener ! FileResult(bytes.length, hash)
      context.stop(self)
    }
    case _ => log.info("Unknown message")
  } 
}

class DirectoryProcessor(listener: ActorRef, supervisor: ActorRef) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case Process(dir) => {
      log.info(" Processing " + dir)
      var files: Stream[Path] = Files.list(dir)
      try {
        val nrOfFiles = files.count
        files.close();

        files = Files.list(dir)
        log.info(" Directory, files: " + nrOfFiles)
        val resultListener = context.actorOf(Props(classOf[ResultListener], listener, supervisor, nrOfFiles), self.path.name + "-r")
        files.forEach(new Consumer[Path]() {
          def accept(path: Path) = {
            val pathName = path.getFileName().toString().filter(isAllowed(_))
            val processor = if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) classOf[DirectoryProcessor] else classOf[FileProcessor]
            val dirRouter = context.actorOf(Props(processor, resultListener, supervisor), pathName)
            dirRouter ! Process(path)
          }
        })
      } finally {
        files.close();
      }
    }
    case _ => log.info("Unknown message")
  }


  def isAllowed(c: Char): Boolean = {
    c.isLetterOrDigit || "-_.*$+:@&=,!~';.".contains(c)
  }
}

