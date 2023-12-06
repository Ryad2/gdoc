/** This class is an abstraction layer to the WebSocket API exposed by
  * JavaScript. It uses Futures to handle asynchronous events.
  */
package cs214.gdoc
package client

import org.scalajs.dom
import scalajs.js
import scalajs.js.timers.setTimeout
import scala.concurrent.{Future, Promise}
import cs214.gdoc.common.*
import cs214.gdoc.common.codec.{Encoder, Decoder}
import cs214.gdoc.common.codec.Codecs.given
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeoutException
import org.scalajs.dom.MessageEvent
import concurrent.ExecutionContext.Implicits.global

class WebSocket(val serverUrl: String)(using messageEncoder: Encoder[Message], messageDecoder: Decoder[Message]):
  var messageListeners: List[Message => Any] = Nil

  var ws: Option[dom.WebSocket] = None
  var paused = false
  var sendBuffer = List[Message]()

  def pause =
    close()
    paused = true
    ws = None
    sendBuffer = Nil

  def unpause: Future[Unit] =
    connect.flatMap(_ =>
      for listener <- messageListeners do innerOnReceive(listener)

      sendBuffer.foreach(send)
      sendBuffer = Nil
      paused = false
      Future.successful(())
    )

  def connect: Future[Unit] =
    require(ws.isEmpty)
    val newWs = new dom.WebSocket(serverUrl)
    val p = Promise[Unit]()

    newWs.addEventListener("open", _ => p.success(()))
    newWs.addEventListener("error", e => p.failure(new Exception(e.toString)))
    newWs.addEventListener("close", _ => ws = None)

    ws = Some(newWs)
    p.future

  def send(m: Message) =
    ws match
      case Some(w)        => w.send(messageEncoder.encode(m))
      case None if paused => sendBuffer = sendBuffer :+ m
      case None           => throw new IllegalStateException("Trying to send a message on a closed WebSocket")

  def close(errorCode: Int = 1000, reason: String = "") =
    ws match
      case Some(w) => w.close(errorCode, reason)
      case None    => ()
    ws = None

  /** Listens when the WebSocket connection dies.
    *
    * @param callback
    */
  def onClose(callback: => Any) =
    ws match
      case Some(w) => w.addEventListener("close", _ => callback)
      case None    => throw new IllegalStateException("Trying to send a message on a closed WebSocket")

  /** This functin is a helper. It will return a Future that will be success as
    * soon as a message satisfying the predicate once the message is received.
    * If the timeout is reached, the Future will be failed.
    *
    * @param predicate
    * @return
    */
  def receiveOnce(predicate: Message => Boolean, t: Option[FiniteDuration]): Future[Message] =
    require(ws.isDefined)
    val Some(w) = ws: @unchecked
    val p = Promise[Message]()
    t.foreach(t =>
      setTimeout(t)(p.failure(new TimeoutException("Timeout")))
    )

    lazy val f: js.Function1[MessageEvent, Unit] = e => eventListener(e)

    def eventListener(e: MessageEvent) =
      val m = messageDecoder.decode(e.data.asInstanceOf[String])
      m.foreach(m =>
        if predicate(m) then
          p.success(m)
          w.removeEventListener("message", f)
      )

    w.addEventListener("message", f)
    p.future

  def receiveOnce(predicate: Message => Boolean, t: FiniteDuration): Future[Message] =
    receiveOnce(predicate, Some(t))

  def receiveOnce: Future[Message] =
    receiveOnce(_ => true, None)

  def onReceive(callback: Message => Any) =
    innerOnReceive(callback)
    messageListeners = callback :: messageListeners

  private def innerOnReceive(callback: Message => Any) =
    require(ws.isDefined)
    val Some(w) = ws: @unchecked
    w.addEventListener[MessageEvent](
      "message",
      e =>
        val m = messageDecoder.decode(e.data.asInstanceOf[String])
        m.foreach(callback)
    )
