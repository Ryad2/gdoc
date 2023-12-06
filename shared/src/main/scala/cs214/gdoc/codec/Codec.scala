package cs214.gdoc
package common
package codec

trait Encoder[A]:
  def encode(a: A): String
trait Decoder[A]:
  def decode(a: String): Option[A]
