package org.stitz.scala.bladerunner.file

import java.security.MessageDigest
import akka.stream.stage._
import akka.util.ByteString
import akka.stream.scaladsl.Source
import scala.concurrent.Future
import java.io.File
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink

object FileDigester {
	implicit val system = ActorSystem("Hashes")
  def hash(file: File) : Future[ByteString] = {
      implicit val materializer = ActorMaterializer()
      val source = FileIO.fromFile(file, 1024)
      val transform = new FileDigester(source).digest
      return transform.runWith(Sink.head)
  }
}
class FileDigester(data: Source[ByteString, Future[Long]]) {
def digestCalculator(algorithm: String) = new PushPullStage[ByteString, ByteString] {
  val digest = MessageDigest.getInstance(algorithm)
 
  override def onPush(chunk: ByteString, ctx: Context[ByteString]): SyncDirective = {
    digest.update(chunk.toArray)
    ctx.pull()
  }
 
  override def onPull(ctx: Context[ByteString]): SyncDirective = {
    if (ctx.isFinishing) ctx.pushAndFinish(ByteString(digest.digest()))
    else ctx.pull()
  }
 
  override def onUpstreamFinish(ctx: Context[ByteString]): TerminationDirective = {
    // If the stream is finished, we need to emit the last element in the onPull block.
    // It is not allowed to directly emit elements from a termination block
    // (onUpstreamFinish or onUpstreamFailure)
    ctx.absorbTermination()
  }
}

  val digest: Source[ByteString, Future[Long]] = data.transform(() => digestCalculator("MD5"))
}