package cs214.gdoc
package common
package codec

import cs214.gdoc.common.*
import cs214.gdoc.common.codec.Encoder
import upickle.default._

import upickle.default.*

object Codecs:
  given Encoder[Message] = write[Message](_)

  given Decoder[Message] = new Decoder[Message] :
    def decode(a: String) : Option[Message] =
      try Some(read[Message](a)) catch case _ : Throwable => None