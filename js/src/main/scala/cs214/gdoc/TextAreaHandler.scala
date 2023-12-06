package cs214.gdoc
package client

import org.scalajs.dom.html
import org.scalajs.dom.InputEvent
import org.scalajs.dom.KeyboardEvent

def handleTextArea(
    t: html.TextArea,
    onCharacterAdd: (Char, Integer) => Any,
    onCharacterDelete: Integer => Any
) =

  def isSelection(e: html.Input): Boolean =
    e.selectionStart != e.selectionEnd

  // Listening on input
  t.addEventListener(
    "input",
    (e: InputEvent) =>
      val c: String = e.data

      if e.data != null then
        val k = e.target
        val position: Int = e.target.asInstanceOf[html.Input].selectionStart - 1
        onCharacterAdd(c(0), position)
  )

  // Delete
  // Listening on backspace and delete key
  t.addEventListener(
    "keydown",
    (e: KeyboardEvent) =>
      if isSelection(e.target.asInstanceOf[html.Input]) then
        e.preventDefault()
      else if e.keyCode == 8 || e.keyCode == 46 then
        val target = e.target.asInstanceOf[html.Input]
        val position = target.selectionStart + (if e.keyCode == 8 then -1 else 0)
        onCharacterDelete(position)
  )
