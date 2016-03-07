package org.stitz.scala.bladerunner.file

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.io.Framing
import akka.stream.scaladsl.{FileIO, Keep}
import akka.util.ByteString

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

trait TransferTransformFile {
  /**
   * @return Number of bytes written
   */
  def run(from: File, to: File): Long
}

object AkkaStreamsTransferTransformFile extends TransferTransformFile {
  private lazy implicit val system = ActorSystem()

  override def run(from: File, to: File) = {
    implicit val mat = ActorMaterializer()

    val r: Future[Long] = FileIO.fromFile(from)
      .via(Framing.delimiter(ByteString("\n"), 1048576))
      .map(_.utf8String)
      .filter(!_.contains("#!@"))
      .map(_.replace("*", "0"))
      .intersperse("\n")
      .map(ByteString(_))
      .toMat(FileIO.toFile(to))(Keep.right)
      .run()

    Await.result(r, 1.hour)
  }

  def shutdown() = {
    system.terminate()
  }
}

object TransferTransformFileRunner extends App {
  def runTransfer(ttf: TransferTransformFile, sizeMB: Int): String = {
    var input : File = null
    val output = File.createTempFile("fft", "txt")
    try {
      ttf.run(input, output).toString
    } finally output.delete()
  }

  val tests = List(
    (AkkaStreamsTransferTransformFile, 10),
    (AkkaStreamsTransferTransformFile, 100),
    (AkkaStreamsTransferTransformFile, 500)
  )

//  runTests(tests.map { case (ttf, sizeMB) =>
//    (s"${if (ttf == ScalazStreamsTransferTransformFile) "scalaz" else "akka"}, $sizeMB MB",
//      () => runTransfer(ttf, sizeMB))
//  }, 3)

  AkkaStreamsTransferTransformFile.shutdown()
}