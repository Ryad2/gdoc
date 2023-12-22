package cs214.gdoc
package common

class TextCRDT(
    val replicaId: String,
    var textList: List[(Char, CharPosition)]
):


  def compare(a: CharPosition, b: CharPosition): Int =
    if a.index.compare(b.index) == 0 then
      if a.sender.compare(b.sender) == 0 then
        a.counter.compare(b.counter)
      else
        a.sender.compare(b.sender)
    else
      a.index.compare(b.index)

  def createBetween(left: Option[CharPosition], right: Option[CharPosition]): CharPosition =
    val counter = textList.count((c, pos) => pos.sender == replicaId)
    (left, right) match
      case (None, None) =>
        CharPosition(replicaId, counter, 0.5)
      case (None, Some(right)) => CharPosition(replicaId, counter, right.index/2.0)
      case (Some(left), None) => CharPosition(replicaId, counter, (left.index+1)/2.0)
      case (Some(left), Some(right)) => CharPosition(replicaId, counter, (left.index + right.index)/2.0)

  def getString(): String =
    textList.map(_._1).mkString


  def insert(c: Char, index: Int): Operation =
    val newCharPosition =
    if index < 0 || index > textList.length || textList.isEmpty  then
      createBetween(None, None)
    else if index == 0 then
      createBetween(None, Some(textList.map(_._2)(index)))
    else if index == textList.length then
      createBetween(Some(textList.map(_._2)(index-1)), None)
    else
      createBetween(Some(textList.map(_._2)(index-1)), Some(textList.map(_._2)(index)) )

    textList = textList.patch(index, List((c, newCharPosition)), 0)
    Insert(c, newCharPosition)

  def delete(index: Int): Operation =
    val delatedCharPosition = textList.map(_._2)(index)
    textList = textList.patch(index, Nil, 1)
    Delete(delatedCharPosition)

  def apply(op: Operation): Unit =
    op match
      case Insert(cInsert, posInsert) =>
        if !textList.map(_._2).contains(posInsert) then
          val index = textList.indexWhere((c, pos) => compare(pos, posInsert) > 0)
          //insert(cInsert, index)
          textList = textList.patch(index, List((cInsert, posInsert)), 0).sortBy(_._2.index)

      case Delete(posInsert) =>
        textList = textList.filter(posInsert != _._2)