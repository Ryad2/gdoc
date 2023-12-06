package cs214.gdoc
package common

class TextCRDTTest extends munit.FunSuite:

  test("compare with two identical CharPositions") {
    val replicaId = "replicaId"
    val textCRDT = new TextCRDT(replicaId, Nil)
    val charPosition1 = CharPosition(replicaId, 0, 0)
    assertEquals(textCRDT.compare(charPosition1, charPosition1), 0)
    val charPosition2 = CharPosition(replicaId, 1, 1)
    assertEquals(textCRDT.compare(charPosition2, charPosition2), 0)
  }

  test("compare with two different CharPositions, different by their index") {
    val replicaId = "replicaId"
    val textCRDT = new TextCRDT(replicaId, Nil)
    val charPosition1 = CharPosition(replicaId, 0, 0)
    val charPosition2 = CharPosition(replicaId, 0, 1)
    assertEquals(textCRDT.compare(charPosition1, charPosition2), -1)
    assertEquals(textCRDT.compare(charPosition2, charPosition1), 1)
    val charPosition3 = CharPosition(replicaId, 0, .25)
    val charPosition4 = CharPosition(replicaId, 0, .251)
    assertEquals(textCRDT.compare(charPosition1, charPosition2), -1)
    assertEquals(textCRDT.compare(charPosition2, charPosition1), 1)
  }

  test("compare with two different CharPosition, different by their sender") {
    val textCRDT = new TextCRDT("1", Nil)
    val charPosition1 = CharPosition("1", 0, 0)
    val charPosition2 = CharPosition("2", 0, 0)
    assertEquals(textCRDT.compare(charPosition1, charPosition2), "1".compare("2"))
    assertEquals(textCRDT.compare(charPosition2, charPosition1), "2".compare("1"))
  }

  test("compare with two different CharPosition, different by their counter") {
    val textCRDT = new TextCRDT("1", Nil)
    val charPosition1 = CharPosition("1", 5, 0)
    val charPosition2 = CharPosition("1", 6, 0)
    assertEquals(textCRDT.compare(charPosition1, charPosition2), -1)
    assertEquals(textCRDT.compare(charPosition2, charPosition1), 1)
  }

  test("createBetween with (None, None) is correct") {
    val textCRDT = new TextCRDT("1", Nil)
    val charPosition = textCRDT.createBetween(None, None)
    assertEquals(charPosition.index, BigDecimal(0.5))
    assertEquals(charPosition.sender, "1")
    assertEquals(charPosition.counter, 0)
  }

  test("createBetween with (None, Some(CharPosition(…, 0.25))) is correct") {
    val textCRDT = new TextCRDT("1", Nil)
    val charPosition = textCRDT.createBetween(None, Some(CharPosition("1", 0, 0.25)))
    assertEquals(charPosition.index, BigDecimal(0.125))
    assertEquals(charPosition.sender, "1")
    assertEquals(charPosition.counter, 0)
  }

  test("createBetween with (Some(CharPosition(…, 0.25), None)) is correct") {
    val textCRDT = new TextCRDT("1", Nil)
    val charPosition = textCRDT.createBetween(Some(CharPosition("1", 0, 0.25)), None)
    assertEquals(charPosition.index, BigDecimal(0.625))
    assertEquals(charPosition.sender, "1")
    assertEquals(charPosition.counter, 0)
  }

  test("createBetween with (Some(CharPosition(…, 0.15)), Some(CharPosition(…, 0.25))) is correct") {
    val textCRDT = new TextCRDT("1", Nil)
    val charPosition = textCRDT.createBetween(Some(CharPosition("1", 0, 0.15)), Some(CharPosition("1", 1, 0.25)))
    assertEquals(charPosition.index, BigDecimal(0.2))
    assertEquals(charPosition.sender, "1")
    assertEquals(charPosition.counter, 0)
  }

  test("createBetween's return counter is correct, as well as the BigDecimal index") {
    val textCRDT = new TextCRDT(
      "1",
      List(
        ('a', CharPosition("1", 0, 0.1)),
        ('b', CharPosition("2", 0, 0.25)),
        ('c', CharPosition("2", 1, 0.25)),
        ('d', CharPosition("1", 1, 0.5))
      )
    )
    val charPosition = textCRDT.createBetween(None, Some(CharPosition("1", 0, 0.1)))
    assertEquals(charPosition.index, BigDecimal(0.05))
    assertEquals(charPosition.sender, "1")
    assertEquals(charPosition.counter, 2)
  }

  test("createBetween's return sender is correct") {
    val textCRDT = new TextCRDT("WhoReadsThis!", Nil)
    val charPosition = textCRDT.createBetween(None, Some(CharPosition("1", 0, 0.1)))
    assertEquals(charPosition.index, BigDecimal(0.05))
    assertEquals(charPosition.sender, "WhoReadsThis!")
    assertEquals(charPosition.counter, 0)
  }

  test("getString is correct, with a valid textList") {
    val textCRDT = new TextCRDT(
      "1",
      List(
        ('a', CharPosition("1", 0, 0.1)),
        ('b', CharPosition("2", 0, 0.25)),
        ('c', CharPosition("2", 1, 0.255)),
        ('d', CharPosition("1", 1, 0.5))
      )
    )
    assertEquals(textCRDT.getString(), "abcd")
  }

  test("insert returns the correct operation") {
    val textCRDT = new TextCRDT(
      "1",
      List(
        ('a', CharPosition("1", 0, 0.1)),
        ('b', CharPosition("2", 0, 0.25)),
        ('c', CharPosition("2", 1, 0.255)),
        ('d', CharPosition("1", 1, 0.5))
      )
    )
    val operation = textCRDT.insert('e', 2)
    assertEquals(operation, Insert('e', CharPosition("1", 2, 0.2525)))
  }

  test("insert modifies properly the string") {
    val textCRDT = new TextCRDT(
      "1",
      List(
        ('a', CharPosition("1", 0, 0.1)),
        ('b', CharPosition("2", 0, 0.25)),
        ('c', CharPosition("2", 1, 0.255)),
        ('d', CharPosition("1", 1, 0.5))
      )
    )
    val operation = textCRDT.insert('e', 2)
    assertEquals(textCRDT.getString(), "abecd")
  }

  test("insert still keeps the textList ordered") {
    val textCRDT = new TextCRDT(
      "1",
      List(
        ('a', CharPosition("1", 0, 0.1)),
        ('b', CharPosition("2", 0, 0.25)),
        ('c', CharPosition("2", 1, 0.255)),
        ('d', CharPosition("1", 1, 0.5))
      )
    )
    val operation = textCRDT.insert('e', 2)
    assertEquals(
      textCRDT.textList,
      List(
        ('a', CharPosition("1", 0, 0.1)),
        ('b', CharPosition("2", 0, 0.25)),
        ('e', CharPosition("1", 2, 0.2525)),
        ('c', CharPosition("2", 1, 0.255)),
        ('d', CharPosition("1", 1, 0.5))
      )
    )
  }

  test("delete returns the correct operation") {
    val textCRDT = new TextCRDT(
      "1",
      List(
        ('a', CharPosition("1", 0, 0.1)),
        ('b', CharPosition("2", 0, 0.25)),
        ('c', CharPosition("2", 1, 0.255)),
        ('d', CharPosition("1", 1, 0.5))
      )
    )
    val operation = textCRDT.delete(2)
    assertEquals(operation, Delete(CharPosition("2", 1, 0.255)))
  }

  test("delete returns the correct operation") {
    val textCRDT = new TextCRDT(
      "1",
      List(
        ('a', CharPosition("1", 0, 0.1)),
        ('b', CharPosition("2", 0, 0.25)),
        ('c', CharPosition("2", 1, 0.255)),
        ('d', CharPosition("1", 1, 0.5))
      )
    )
    val operation = textCRDT.delete(2)
    assertEquals(operation, Delete(CharPosition("2", 1, 0.255)))
  }

  test("delete correctly removes the char from the string that is returned by getString()") {
    val textCRDT = new TextCRDT(
      "1",
      List(
        ('a', CharPosition("1", 0, 0.1)),
        ('b', CharPosition("2", 0, 0.25)),
        ('c', CharPosition("2", 1, 0.255)),
        ('d', CharPosition("1", 1, 0.5))
      )
    )
    val operation = textCRDT.delete(2)
    assertEquals(textCRDT.getString(), "abd")
  }

  test("apply correctly inserts a char in the textList") {
    val textCRDT = new TextCRDT("1", Nil)
    textCRDT.apply(Insert('a', CharPosition("1", 0, 0.1)))
    assertEquals(textCRDT.textList, List(('a', CharPosition("1", 0, 0.1))))
    assertEquals(textCRDT.getString(), "a")
  }

  test("apply ignores duplicates") {
    val initialTextList = List(
      ('a', CharPosition("1", 0, 0.1)),
      ('b', CharPosition("2", 0, 0.25)),
      ('c', CharPosition("2", 1, 0.255)),
      ('d', CharPosition("1", 1, 0.5))
    )
    val textCRDT = new TextCRDT("1", initialTextList)
    textCRDT.apply(Insert('a', CharPosition("1", 0, 0.1)))
    assertEquals(textCRDT.textList, initialTextList)
    assertEquals(textCRDT.getString(), "abcd")
  }

  test("apply correctly deletes a CharPosition in the textList") {
    val initialTextList = List(
      ('a', CharPosition("1", 0, 0.1)),
      ('b', CharPosition("2", 0, 0.25)),
      ('c', CharPosition("2", 1, 0.255)),
      ('d', CharPosition("1", 1, 0.5))
    )
    val toDelete = initialTextList(2)._2
    val textCRDT = new TextCRDT("1", initialTextList)
    textCRDT.apply(Delete(toDelete))
    assertEquals(textCRDT.textList, initialTextList.take(2) ++ initialTextList.drop(3))
    assertEquals(textCRDT.getString(), "abd")
  }

  // This test creates random operations (insert, delete) on the CRDT
  // utilizing the `createBetween`, `insert`, `delete` and `apply` methods.
  // If a conflict occurs, the test will fail, while giving some details.
  // Format of the clues:
  // Previous textList: <textList>
  // Previous string: <string given by .toString()>
  // When <insert/delete> the char '<char>' at index <index> from <user/self>, the textList is:
  // <new textList>
  // which resulted a different string than expected (<expected>): <actual>.
  //
  // <user/self> is either a User (refered by its replicaId), or "self" (the current replicaId)
  // <user> is applied with .apply and <self> is applied with .insert/.delete
  test("TextCRDT works perfectly with a totally randomized test, totally random operations.") {
    val textCRDT = new TextCRDT("1", Nil)
    val seed = 170

    val random = scala.util.Random(seed)

    def randomChar = random.nextPrintableChar()
    def randomIndex(length: Int) = random.nextInt(length)
    def randomUser = List("Clément", "Samuel", "Martin", "Viktor", "Marwan", "Hamza", "Matt").apply(randomIndex(7))

    def generateClue(
        previousTL: List[(Char, CharPosition)],
        previousStr: String,
        newTL: List[(Char, CharPosition)],
        newStr: String,
        expectedStr: String,
        opString: String,
        index: Int,
        user: String,
        char: Char
    ): String =
      // I like the "f" flag, and I do not like the | with the .stripMargin, yes, this is personal
      // At least it gives the correct output? :D
      f"Previous textList: $previousTL\nPrevious string: $previousStr\nWhen $opString the char '$char' at index $index from $user, the textList is:\n$newTL\nwhich resulted a different string than expected ($expectedStr): $previousStr."

    var string = ""
    for i <- 0 until 1000 do
      // Either insert, or delete
      val previousTextList = textCRDT.textList
      val previousString = textCRDT.getString()

      if random.nextBoolean() then
        // Insert
        val index = randomIndex(string.length + 1)
        val char = randomChar
        // Either self, or other
        if random.nextBoolean() then
          // Self
          textCRDT.insert(char, index)
          string = string.take(index) + char + string.drop(index)
          assertEquals(
            textCRDT.getString(),
            string,
            generateClue(
              previousTextList,
              previousString,
              textCRDT.textList,
              textCRDT.getString(),
              string,
              "insert",
              index,
              "self",
              char
            )
          )
        else
          // Other (random user)
          val user = randomUser
          val otherUserCRDT = new TextCRDT(user, previousTextList)
          val externalOperation = otherUserCRDT.insert(char, index)
          string = string.take(index) + char + string.drop(index)
          textCRDT.apply(externalOperation)
          assertEquals(
            textCRDT.getString(),
            string,
            generateClue(
              previousTextList,
              previousString,
              textCRDT.textList,
              textCRDT.getString(),
              string,
              "insert",
              index,
              user,
              char
            )
          )
      else if string.nonEmpty then
        // Delete (only if string is not empty)
        val index = randomIndex(string.length)
        // Either self, or other
        if random.nextBoolean() then
          // Self
          textCRDT.delete(index)
          string = string.take(index) + string.drop(index + 1)
          assertEquals(
            textCRDT.getString(),
            string,
            generateClue(
              previousTextList,
              previousString,
              textCRDT.textList,
              textCRDT.getString(),
              string,
              "delete",
              index,
              "self",
              string(index)
            )
          )
        else
          // Other (random user)
          val user = randomUser
          val otherUserCRDT = new TextCRDT(user, previousTextList)
          val externalOperation = otherUserCRDT.delete(index)
          string = string.take(index) + string.drop(index + 1)
          textCRDT.apply(externalOperation)
          assertEquals(
            textCRDT.getString(),
            string,
            generateClue(
              previousTextList,
              previousString,
              textCRDT.textList,
              textCRDT.getString(),
              string,
              "delete",
              index,
              user,
              string(index)
            )
          )
  }
