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
  var clients: Map[String, (WebSocket, Int, Boolean)] = Map.empty
  var operations: Vector[Operation] = Vector.empty

  override def onStart () =
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
    messageDecoder.decode(content) match
      case Some(GetConnectedClients) =>
        ws.send(messageEncoder.encode(ConnectedClients(clients.filter((_,value) => value._3).keys.toList)))

      case Some(DisconnectRequest) =>
        clients = clients.removed(clients.find(_._2._1 == ws).get._1)
        ws.close()
      case Some(InitialEvent(username)) =>
        if clients.contains(username) then
          ws.send(messageEncoder.encode(UsernameTaken))
          ws.close()
        else
          ws.send(messageEncoder.encode(InitialEventOk))
          operations.foreach(op => ws.send(messageEncoder.encode(TextOperation(op))) )
          clients = clients.updated(username, (ws, operations.length, true))

      case Some(TextOperation(op))  =>
        operations = operations :+ op
        clients = clients.map((username, value) =>
          if value._3 then (username, (value._1, value._2 + 1, true))
          else (username, value))

        clients.filter((_, value) => value._3).foreach( (username, value) =>
          value._1.send(messageEncoder.encode(TextOperation(op)))
        )


      case Some(ConnectedClients(clients)) =>
      case Some(FetchMissedOperations(username)) =>
        val missingOps = operations.slice(operations.length-clients(username)._2, operations.length)
        clients = clients.updated(username, (clients(username)._1, operations.length, true))
        missingOps.foreach(op => ws.send(messageEncoder.encode(TextOperation(op))))
        
        
      case _ => None


@main def main =
  val s: WebSocketServer = CRDTServer(8080)
  s.setReuseAddr(true)
  s.run()
