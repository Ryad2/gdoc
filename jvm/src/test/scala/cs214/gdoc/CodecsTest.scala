package cs214.gdoc
package common
package codec

import cs214.gdoc.common.codec.Codecs.given

class CodecsTest extends munit.FunSuite:
  val encoder: Encoder[Message] = summon[Encoder[Message]]
  val decoder: Decoder[Message] = summon[Decoder[Message]]
  val special = "éèàçù;*-+&v^$ù%µ¡÷×¿¬…¡!\"#¤%/&()=?^`{[|@]}¨£¤¥§©ª«¬®¯°±²³´µ¶·¸¹'º»¼½¾¿".split("")

  def isSymmetric(a: Message) =
    assertEquals(decoder.decode(encoder.encode(a)), Some(a), f"$a is not symmetric.")

  test("Encoder/Decoder is symmetric with objects") {
    isSymmetric(DisconnectRequest)
    isSymmetric(UsernameTaken)
    isSymmetric(GetConnectedClients)
    isSymmetric(InitialEventOk)
  }

  test("Encoder/Decoder is symmetric with TextOperation('a', CharPosition('a', 0, 0.0))") {
    isSymmetric(TextOperation(Insert('a', CharPosition("a", 0, BigDecimal(0)))))
    isSymmetric(TextOperation(Delete(CharPosition("a", 0, BigDecimal(0)))))
  }

  test("Encoder/Decoder is symmetric with TextOperation with some special characters") {
    special.foreach { c =>
      isSymmetric(TextOperation(Insert(c.toCharArray()(0), CharPosition(c, 0, BigDecimal(0)))))
      isSymmetric(TextOperation(Delete(CharPosition(c, 0, BigDecimal(0)))))
    }
  }

  test("Encoder/Decoder is symmetric with ConnectedClients") {
    isSymmetric(ConnectedClients(List("a", "b", "c")))
    isSymmetric(ConnectedClients(List("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")))
  }

  test("Encoder/Decoder is symmetric with ConnectedClients that have special characters") {
    isSymmetric(ConnectedClients(special.toList))
  }

  test("Encoder/Decoder is symmetric with FetchMissedOperations") {
    isSymmetric(FetchMissedOperations("a"))
    isSymmetric(FetchMissedOperations("b"))
    isSymmetric(FetchMissedOperations("c"))
  }

  test("Encoder/Decoder is symmetric with FetchMissedOperations that have special characters") {
    isSymmetric(FetchMissedOperations(special.mkString("")))
  }
