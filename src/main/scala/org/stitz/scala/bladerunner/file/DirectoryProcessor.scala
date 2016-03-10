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
import akka.routing.SmallestMailboxPool

sealed trait DirectoryMessage
case class Process(path: Path, resultListener: ActorRef) extends DirectoryMessage
 
class FileProcessor extends Actor {
  val log = Logging(context.system, this)
  def receive = {
    case Process(path, resultListener) => {
      Supervisor.resultProcessor ! StartedOn(path)
      val bytes = Files.readAllBytes(path)
      val hash = MurmurHash3.bytesHash(bytes)
      resultListener ! FileResult(path, bytes.length, hash)
    }
    case _ => log.info("Unknown message")
  } 
}

class DirectoryProcessor extends Actor {
  val log = Logging(context.system, this)


  def receive = {
    case Process(dir, listener) => {
      log.info(" Processing " + dir)
      Supervisor.resultProcessor ! StartedOn(dir)
      var files: Stream[Path] = Files.list(dir)
      try {
        val nrOfFiles = files.count.asInstanceOf[Int]
        files.close();

        files = Files.list(dir)
        log.info(" Directory, files: " + nrOfFiles)
        val resultListener = context.actorOf(Props(classOf[ResultListener], listener, dir, nrOfFiles))
        files.forEach(new Consumer[Path]() {
          def accept(path: Path) = {
            val processor = if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) Supervisor.dirProcessor else Supervisor.fileProcessor
            processor ! Process(path, resultListener)
          }
        })
      } finally {
        files.close();
      }
    }
    case _ => log.info("Unknown message")
  }
}

