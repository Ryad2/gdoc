
import cs214.gdoc.common.*
import cs214.gdoc.common.codec.{Decoder, Encoder}
import upickle.default.*
import cs214.gdoc.common.codec.Codecs.given_Encoder_Message
import cs214.gdoc.common.codec.Codecs.given_Decoder_Message


val message1 = GetConnectedClients
val message2 = UsernameTaken
val message3 = DisconnectRequest
val message4 = InitialEventOk
val message5 = InitialEvent("Ryad")
val message6 = ConnectedClients(List("rio", "rioa"))
val message7 = FetchMissedOperations("rikao")


summon[Encoder[Message]].encode (message1)
summon[Encoder[Message]].encode (message2)
summon[Encoder[Message]].encode (message3)
summon[Encoder[Message]].encode (message4)
summon[Encoder[Message]].encode (message5)
summon[Encoder[Message]].encode (message6)
summon[Encoder[Message]].encode (message7)
