package cs214.gdoc
package common

import upickle.default.ReadWriter

final case class CharPosition(sender: String, counter: Int, index: BigDecimal)
    derives ReadWriter

