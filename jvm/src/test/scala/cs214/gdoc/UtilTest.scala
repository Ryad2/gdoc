package cs214.gdoc
package tests

import cs214.gdoc.common.*
import cs214.gdoc.common.codec.*
import cs214.gdoc.common.codec.Codecs.given

import org.java_websocket.WebSocket
import scala.reflect.ClassTag

object UtilTest:
  extension [U](u: U)
    inline def assertInstanceOf[V]: V =
      assert(u.isInstanceOf[V])
      u.asInstanceOf[V]

  class MockWebSocket(val id: String)(using decoder: Decoder[Message]) extends WebSocket:
    var closed = false
    var received = List[Message]()

    def clear =
      received = List[Message]()

    override def close(code: Int, message: String): Unit = closed = true
    override def close(): Unit = closed = true
    override def close(code: Int): Unit = closed = true
    override def send(text: String): Unit =
      decoder.decode(text) match
        case Some(msg) => received = msg :: received
        case None      => ()

    override def send(bytes: Array[Byte]): Unit = ???
    override def sendPing(): Unit = ()
    override def getRemoteSocketAddress(): java.net.InetSocketAddress = ???
    override def getLocalSocketAddress(): java.net.InetSocketAddress = ???
    override def getReadyState(): org.java_websocket.enums.ReadyState =
      if closed then
        org.java_websocket.enums.ReadyState.CLOSED
      else
        org.java_websocket.enums.ReadyState.OPEN

    override def isClosing(): Boolean = false
    override def isFlushAndClose(): Boolean = false
    override def isClosed(): Boolean = closed
    override def isOpen(): Boolean = !closed
    override def toString(): String = id
    override def hashCode(): Int = id.hashCode()
    override def equals(obj: Any): Boolean =
      obj match
        case other: MockWebSocket => other.id == id
        case _                    => false
    override def getAttachment[T](): T = ???
    override def setAttachment[T](attachment: T): Unit = ()
    def closeConnection(x$0: Int, x$1: String): Unit = ???
    def getDraft(): org.java_websocket.drafts.Draft = ???
    def getProtocol(): org.java_websocket.protocols.IProtocol = ???
    def getResourceDescriptor(): String = ???
    def getSSLSession(): javax.net.ssl.SSLSession = ???
    def hasBufferedData(): Boolean = ???
    def hasSSLSupport(): Boolean = ???
    def send(x$0: java.nio.ByteBuffer): Unit = ???
    def sendFragmentedFrame(x$0: org.java_websocket.enums.Opcode, x$1: java.nio.ByteBuffer, x$2: Boolean): Unit = ???
    def sendFrame(x$0: org.java_websocket.framing.Framedata): Unit = ???
    def sendFrame(x$0: java.util.Collection[org.java_websocket.framing.Framedata]): Unit = ???
