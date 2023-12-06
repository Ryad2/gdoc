package cs214.gdoc
package common

import upickle.default.ReadWriter

sealed trait Message derives ReadWriter
case object GetConnectedClients extends Message
case object UsernameTaken extends Message
case object DisconnectRequest extends Message
case object InitialEventOk extends Message
case class InitialEvent(username: String) extends Message
case class TextOperation(op: Operation) extends Message
case class ConnectedClients(users: List[String]) extends Message
case class FetchMissedOperations(username: String) extends Message
