package org.stitz.scala.bladerunner.file

import java.nio.file.Path

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString

import scala.concurrent.Future

object FileDigester {
	implicit val system = ActorSystem("Hashes")

  def hash(path: Path): Future[ByteString] = {
    implicit val materializer = ActorMaterializer()

    return FileIO.fromPath(path, 1024).via(DigestCalculator.hash(Algorithm.MD5)).runWith(Sink.head[ByteString])
  }
}