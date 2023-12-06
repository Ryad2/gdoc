package cs214.gdoc
package common

import upickle.default.ReadWriter

sealed trait Operation(val pos: CharPosition) derives ReadWriter
case class Insert(c: Char, _pos: CharPosition) extends Operation(_pos)
case class Delete(_pos: CharPosition) extends Operation(_pos)
