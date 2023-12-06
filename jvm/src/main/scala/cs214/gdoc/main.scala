package cs214.gdoc
package server

import java.net.InetSocketAddress

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

import cs214.gdoc.common.TextCRDT
import cs214.gdoc.common.*

import scala.jdk.CollectionConverters.*
import cs214.gdoc.common.codec.{Encoder, Decoder}
import cs214.gdoc.common.codec.Codecs.given
import javax.naming.InitialContext

class CRDTServer(val port: Int)
    extends WebSocketServer(new InetSocketAddress("0.0.0.0", port)):

  private var yourVariable: String = "Initial value"

  override def onStart() =
    println(f"Server started on port $port")

  override def onError(ws: WebSocket, e: Exception) = ()

  override def onClose(
      ws: WebSocket,
      code: Int,
      reason: String,
      remote: Boolean
  ) = ()

  override def onOpen(ws: WebSocket, hs: ClientHandshake) = ()

  // does nothing
  // not supported in this lab.
  // for those who are interested: WebSocket supports not only sending strings
  // but also byte arrays! Useful for files, etc.
  override def onMessage(ws: WebSocket, content: java.nio.ByteBuffer): Unit = ()

  override def onMessage(ws: WebSocket, content: String) =
    onMessageGiven(ws, content)

  def onMessageGiven(ws: WebSocket, content: String)(using
      messageEncoder: Encoder[Message],
      messageDecoder: Decoder[Message]
  ) =
    ???

@main def main =
  val s: WebSocketServer = CRDTServer(8080)
  s.setReuseAddr(true)
  s.run()
