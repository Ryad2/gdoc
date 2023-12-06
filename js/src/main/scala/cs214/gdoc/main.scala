package cs214.gdoc
package client

import scala.scalajs.js

@main def main =
  if js.eval("typeof window") == "object" then
    BrowserMain.main
  else
    println("Why are you executing me with Node.JS?! You are supposed to open ./www/index.html from your browser >:(")
    println("Someone did not read carefully the handout!!!!!")
