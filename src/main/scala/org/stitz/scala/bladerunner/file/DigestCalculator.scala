package org.stitz.scala.bladerunner.file


import akka.stream._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.stage._
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent.Future
import scala.util.{Success, Try}

final case class DigestResult(messageDigest: ByteString, status: Try[Done])

sealed trait Algorithm

object Algorithm {

  case object MD2 extends Algorithm

  case object MD5 extends Algorithm

  case object `SHA-1` extends Algorithm

  case object `SHA-256` extends Algorithm

  case object `SHA-384` extends Algorithm

  case object `SHA-512` extends Algorithm

}

/**
  * The DigestCalculator transforms/digests a stream of akka.util.ByteString to a
  * DigestResult according to a given Algorithm
  */
object DigestCalculator {

  def sink(algorithm: Algorithm): Sink[ByteString, Future[DigestResult]] =
    flow(algorithm).toMat(Sink.head)(Keep.right)

  def flow(algorithm: Algorithm): Flow[ByteString, DigestResult, NotUsed] =
    apply(algorithm)

  def apply(algorithm: Algorithm): Flow[ByteString, DigestResult, NotUsed] =
    Flow.fromGraph[ByteString, DigestResult, NotUsed](new DigestCalculator(algorithm))

  def source(algorithm: Algorithm, input: ByteString): Source[ByteString, NotUsed] =
    Source.single(input).via(hash(algorithm))

  /**
    * Returns the ByteString representation of the digested stream of [[akka.util.ByteString]]
    */
  def hash(algorithm: Algorithm): Flow[ByteString, ByteString, NotUsed] =
    flow(algorithm).map(res => res.messageDigest)
}

private[file] class DigestCalculator(algorithm: Algorithm) extends GraphStage[FlowShape[ByteString, DigestResult]] {
  override val shape: FlowShape[ByteString, DigestResult] = FlowShape.of(in, out)
  val in: Inlet[ByteString] = Inlet("DigestCalculator.in")
  val out: Outlet[DigestResult] = Outlet("DigestCalculator.out")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    val digest = java.security.MessageDigest.getInstance(algorithm.toString)

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        pull(in)
      }
    })

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val chunk = grab(in)
        digest.update(chunk.toArray)
        pull(in)
      }

      override def onUpstreamFinish(): Unit = {
        emit(out, DigestResult(ByteString(digest.digest()), Success(Done)))
        completeStage()
      }
    })
  }
}
