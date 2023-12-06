package cs214.gdoc
package common

class TextCRDT(
    val replicaId: String,
    var textList: List[(Char, CharPosition)]
):


  def compare(a: CharPosition, b: CharPosition): Int =
    ???

  def createBetween(left: Option[CharPosition], right: Option[CharPosition]): CharPosition =
    ???

  def getString(): String =
    ???

  def insert(c: Char, index: Int): Operation =
    ???

  def delete(index: Int): Operation =
    ???

  def apply(op: Operation): Unit =
    ???
