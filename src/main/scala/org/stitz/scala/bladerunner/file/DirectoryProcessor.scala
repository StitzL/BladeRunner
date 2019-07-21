package org.stitz.scala.bladerunner.file

import java.nio.file.{Files, LinkOption, Path}
import java.util.function.Consumer
import java.util.stream.Stream

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import org.stitz.scala.bladerunner.app.ResultController

import scala.util.{Failure, Success}

sealed trait DirectoryMessage
case class Process(path: Path, resultListener: ActorRef) extends DirectoryMessage
 
class FileProcessor extends Actor {

  import context.dispatcher
  val log = Logging(context.system, this)
  def receive = {
    case Process(path, resultListener) => {
//      Supervisor.resultProcessor ! StartedOn(path)
      ResultController.addItem(path)
      val file = path.toFile()

      FileDigester.hash(path).onComplete(
        {
          case Success(hash) =>
            resultListener ! FileResult(path, file.length(), file.lastModified(), hash)

          case Failure(exception) =>
            log.info("Unknown message")

        })
    }
  }
}

class DirectoryProcessor extends Actor {
  val log = Logging(context.system, this)


  def receive = {
    case Process(dir, listener) => {
      log.info(" Processing " + dir)
//      Supervisor.resultProcessor ! StartedOn(dir)
      ResultController.addItem(dir)
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

