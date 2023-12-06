package cs214.gdoc
package server

import cs214.gdoc.common.*
import cs214.gdoc.common.codec.*
import cs214.gdoc.common.codec.Codecs.given
import munit.GenericBeforeEach
import scala.concurrent.Future
import cs214.gdoc.tests.UtilTest.*

class CRDTServerTest extends munit.FunSuite:

  val encoder = summon[Encoder[Message]]
  val decoder = summon[Decoder[Message]]

  var server: CRDTServer = null
  override def beforeEach(context: GenericBeforeEach[Future[Any]]): Unit =
    server = CRDTServer(1)

  def simulateSend(ws: MockWebSocket, msg: Message): Unit =
    server.onMessage(ws, encoder.encode(msg))

  def simulateClose(ws: MockWebSocket): Unit =
    server.onClose(ws, 1000, "Mock", true)
    ws.close()

  test("The server answers correctly for a GetConnectedClients message with a single connected client.") {
    val mock = MockWebSocket("a")
    server.onOpen(mock, null)

    simulateSend(mock, InitialEvent("a"))
    simulateSend(mock, GetConnectedClients)
    assert(mock.received.nonEmpty)
    assertEquals(mock.received.head, ConnectedClients(List("a")))
  }

  test(
    "The server answers correctly for a GetConnectedClients message with multiple connected clients, and do not return clients that have sent a DisconnectRequest."
  ) {
    val mocks = "abc".split("").map(MockWebSocket(_))
    mocks.foreach(server.onOpen(_, null))
    mocks.foreach(mock => simulateSend(mock, InitialEvent(mock.id)))
    simulateSend(mocks(0), GetConnectedClients)
    val ConnectedClients(connectedClients) = mocks(0).received.head: @unchecked
    assert(connectedClients.contains("a"))
    assert(connectedClients.contains("b"))
    assert(connectedClients.contains("c"))
    simulateSend(mocks(1), DisconnectRequest)
    simulateSend(mocks(0), GetConnectedClients)
    val ConnectedClients(connectedClients2) = mocks(0).received.head: @unchecked
    assert(connectedClients2.contains("a"))
    assert(!connectedClients2.contains("b"))
    assert(connectedClients2.contains("c"))
  }

  test("The server answers with UsernameTaken if the username is already taken, and closes the websocket.") {
    val mock1 = MockWebSocket("a")
    val mock2 = MockWebSocket("b")
    server.onOpen(mock1, null)
    server.onOpen(mock2, null)

    simulateSend(mock1, InitialEvent("a"))
    simulateSend(mock2, InitialEvent("a"))
    assert(mock2.received.nonEmpty)
    assertEquals(mock2.received.head, UsernameTaken)
    assert(mock2.closed)
  }

  test("The server answers with InitialEventOk if the username is not taken.") {
    val mock = MockWebSocket("a")
    server.onOpen(mock, null)

    simulateSend(mock, InitialEvent("a"))
    assert(mock.received.nonEmpty)
    assertEquals(mock.received.head, InitialEventOk)
    assert(!mock.closed)
  }

  test("Every TextOperation is broadcasted to everyone (sender optional)") {
    val mocks = "abc".split("").map(MockWebSocket(_))
    mocks.foreach(server.onOpen(_, null))
    mocks.foreach(mock => simulateSend(mock, InitialEvent(mock.id)))
    mocks.foreach(_.clear)
    simulateSend(mocks(0), TextOperation(Insert('a', CharPosition("a", 0, BigDecimal(0.5)))))
    // The two other clients have received the message.
    assertEquals(mocks(1).received.size, 1)
    assertEquals(mocks(2).received.size, 1)
    // Checking content of the message
    val op1 = mocks(1).received.head.assertInstanceOf[TextOperation]
    val op2 = mocks(2).received.head.assertInstanceOf[TextOperation]

    assertEquals(op1, TextOperation(Insert('a', CharPosition("a", 0, BigDecimal(0.5)))))
    assertEquals(op2, TextOperation(Insert('a', CharPosition("a", 0, BigDecimal(0.5)))))
  }

  test("Every TextOperation that has been sent is re-sent to new users") {
    val mock1 = MockWebSocket("a")
    server.onOpen(mock1, null)
    simulateSend(mock1, InitialEvent("a"))
    val operations = List(
      TextOperation(Insert('a', CharPosition("a", 0, BigDecimal(0.5)))),
      TextOperation(Insert('b', CharPosition("a", 0, BigDecimal(0.75)))),
      TextOperation(Insert('c', CharPosition("a", 0, BigDecimal(0.80)))),
      TextOperation(Delete(CharPosition("a", 0, BigDecimal(0.75))))
    )
    operations.foreach(op => simulateSend(mock1, op))
    val mock2 = MockWebSocket("b")
    server.onOpen(mock2, null)
    simulateSend(mock2, InitialEvent("b"))
    assertEquals(mock2.received.filter(_.isInstanceOf[TextOperation]).size, operations.size)
  }

  test("Every TextOperation that has been sent is re-sent to new users") {
    val mock1 = MockWebSocket("a")
    server.onOpen(mock1, null)
    simulateSend(mock1, InitialEvent("a"))
    val operations = List(
      TextOperation(Insert('a', CharPosition("a", 0, BigDecimal(0.5)))),
      TextOperation(Insert('b', CharPosition("a", 0, BigDecimal(0.75)))),
      TextOperation(Insert('c', CharPosition("a", 0, BigDecimal(0.80)))),
      TextOperation(Delete(CharPosition("a", 0, BigDecimal(0.75))))
    )
    operations.foreach(op => simulateSend(mock1, op))
    val mock2 = MockWebSocket("b")
    server.onOpen(mock2, null)
    simulateSend(mock2, InitialEvent("b"))
    assertEquals(mock2.received.filter(_.isInstanceOf[TextOperation]).size, operations.size)
  }

  test("Every TextOperation that has been sent is re-sent to new users") {
    val mock1 = MockWebSocket("a")
    server.onOpen(mock1, null)
    simulateSend(mock1, InitialEvent("a"))
    val operations = List(
      TextOperation(Insert('a', CharPosition("a", 0, BigDecimal(0.5)))),
      TextOperation(Insert('b', CharPosition("a", 0, BigDecimal(0.75)))),
      TextOperation(Insert('c', CharPosition("a", 0, BigDecimal(0.80)))),
      TextOperation(Delete(CharPosition("a", 0, BigDecimal(0.75))))
    )
    operations.foreach(op => simulateSend(mock1, op))
    val mock2 = MockWebSocket("b")
    server.onOpen(mock2, null)
    simulateSend(mock2, InitialEvent("b"))
    assertEquals(mock2.received.filter(_.isInstanceOf[TextOperation]).size, operations.size)
  }

  test("A client that missed some TextOperations will receive them when sending a FetchMissedOperations message.") {
    val mock1 = MockWebSocket("a")
    val mock2 = MockWebSocket("b")
    server.onOpen(mock1, null)
    server.onOpen(mock2, null)
    simulateSend(mock1, InitialEvent("a"))
    simulateSend(mock2, InitialEvent("b"))
    simulateClose(mock2)
    mock2.clear
    val operations = List(
      TextOperation(Insert('a', CharPosition("a", 0, BigDecimal(0.5)))),
      TextOperation(Insert('b', CharPosition("a", 0, BigDecimal(0.75)))),
      TextOperation(Insert('c', CharPosition("a", 0, BigDecimal(0.80)))),
      TextOperation(Delete(CharPosition("a", 0, BigDecimal(0.75))))
    )
    operations.foreach(op => simulateSend(mock1, op))
    val mock3 = MockWebSocket("b-bis")
    simulateSend(mock3, FetchMissedOperations("b"))
    assertEquals(mock3.received.filter(_.isInstanceOf[TextOperation]).size, operations.size)
  }
