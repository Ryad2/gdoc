package cs214.gdoc
package client

import org.scalajs.dom.*
import org.scalajs.dom.{WebSocket as DomWebSocket}
import org.scalajs.dom.html
import scala.scalajs.js.timers.*
import scala.util.Random
import cs214.gdoc.client.UserUtil.*
import cs214.gdoc.common.codec.Codecs.given
import cs214.gdoc.common.*
import cs214.gdoc.client.WebSocket
import scalatags.Text.all.*
import scala.util.Success
import scala.util.Failure
import concurrent.ExecutionContext.Implicits.global

object BrowserMain:
  object quickDom:
    // A quick way to call document.querySelector
    def qs[T](query: String) = document.querySelector(query).asInstanceOf[T]
    def qss(query: String) = document.querySelectorAll(query).toList

    val textarea = qs[html.TextArea]("textarea")
    val username = qs[html.Input]("#username")
    val serverUrl = qs[html.Input]("#url")
    val randomButton = qs[html.Anchor]("#random")
    val connect = qs[html.Button]("#connect")
    val result = qs[html.Paragraph]("#result > p")
    val pages = qss("body > .page")
    val usernameRecall = qs[html.Span]("#username-recall")
    val usernamePicture = qs[html.Image]("#username-picture p")
    val usernamePictureContainer = qs[html.Div]("#username-picture .user-picture")
    val connectedUsers = qs[html.Div]("#connected-users")
    val pauseConnection = qs[html.Button]("#pause-connection")
    val disconnect = qs[html.Button]("#disconnect")

  object ui:
    def showPage(s: String) =
      quickDom.pages.foreach(_.classList.add("hidden"))
      quickDom.qs[html.Div](f"body > #$s-page").classList.remove("hidden")

    def setCurrentUserDetails(uname: String, color: HSLColor, emoji: String) =
      quickDom.usernameRecall.innerText = uname
      quickDom.usernameRecall.style.color = color.toCSS
      quickDom.usernamePictureContainer.style.backgroundColor = color.toCSS
      quickDom.usernamePicture.innerText = emoji

    def showUsers(details: List[(String, HSLColor, String)]) =
      val nodes =
        (for (uname, colors, emoji) <- details
        yield div(
          cls := "user-picture",
          style := s"background-color: ${colors.toCSS}",
          title := uname,
          p(
            cls := "user-picture",
            emoji
          )
        ).render).mkString
      // worst way to do that but im tired
      quickDom.connectedUsers.innerHTML = nodes

    def showConnectResult(s: String) =
      quickDom.result.innerHTML = s
    def blockConnect =
      quickDom.connect.disabled = true
    def unblockConnect =
      quickDom.connect.disabled = false

  def randomUsername =
    quickDom.username.value = getRandomUsername

  def initializeApp(ws: WebSocket, uname: String) =
    val usernameColor = generateColorForUsername(uname)
    ui.setCurrentUserDetails(uname, usernameColor, getUsernameEmoji(uname))
    val crdt = TextCRDT(uname, Nil)
    ui.showPage("app")
    quickDom.textarea.value = crdt.getString()

    // Listening for user inputs
    handleTextArea(
      quickDom.textarea,
      (char, pos) =>
        val op = crdt.insert(char, pos)
        ws.send(TextOperation(op))
      ,
      pos =>
        val op = crdt.delete(pos)
        ws.send(TextOperation(op))
    )

    // Listening the WS
    ws.onReceive({
      case TextOperation(op) =>
        crdt(op)
        quickDom.textarea.value = crdt.getString()
      case ConnectedClients(users) =>
        val details = users.filter(_ != uname).map(uname =>
          (uname, generateColorForUsername(uname), getUsernameEmoji(uname))
        )
        ui.showUsers(details)
      case _ => ()
    })

    var paused = false
    quickDom.pauseConnection.addEventListener(
      "click",
      _ =>
        if paused then
          quickDom.disconnect.disabled = false
          quickDom.pauseConnection.innerText = "Pause connection"
          paused = false
          ws.unpause.map(_ => ws.send(FetchMissedOperations(uname)))
        else
          quickDom.disconnect.disabled = true
          quickDom.pauseConnection.innerText = "Restart connection"
          paused = true
          ws.pause
    )

    setInterval(5000):
      if !paused then
        ws.send(GetConnectedClients)

    quickDom.disconnect.addEventListener(
      "click",
      _ =>
        ws.send(DisconnectRequest)
        ws.close()
        ui.showPage("disconnected")
    )

  // State
  var isConnecting = false
  def tryConnect: Unit =
    if isConnecting then
      return ()
    isConnecting = true
    ui.blockConnect
    val username = quickDom.username.value
    val server = quickDom.serverUrl.value
    val ws = new WebSocket(server)

    ws.connect.onComplete {
      case Success(_) =>
        println("WS: Connected to server… Sending initial request.")
        ws.send(InitialEvent(username))
        setTimeout(1000):
          if isConnecting then
            isConnecting = false
            ui.unblockConnect
            ui.showConnectResult("Server did not answer… Contact a TA.")
            ws.close(-1, "Did not receive an answer in time.")

      case Failure(_) =>
        isConnecting = false
        ui.unblockConnect
        ui.showConnectResult(f"Have you launched the server? Check the <i>Console</i> for more details.")
    }

    ws.onReceive {
      case UsernameTaken =>
        isConnecting = false
        ui.unblockConnect
        ui.showConnectResult("Username already taken.")
        ws.close()
      case InitialEventOk =>
        isConnecting = false
        initializeApp(ws, username)
      case _ => ()
    }

  def main =
    ui.showPage("connect")
    randomUsername
    quickDom.randomButton.addEventListener("click", _ => randomUsername)
    quickDom.connect.addEventListener(
      "click",
      _ =>
        tryConnect
    )
