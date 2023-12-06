package cs214.gdoc
package client

import scala.util.Random

object UserUtil:
  val animalsEmojis = Map(
    "Monkey" -> "🐒",
    "Gorilla" -> "🦍",
    "Orangutan" -> "🦧",
    "Dog" -> "🐕",
    "Poodle" -> "🐩",
    "Raccoon" -> "🦝",
    "Cat" -> "🐈",
    "Lion" -> "🦁",
    "Tiger" -> "🐯",
    "Leopard" -> "🐆",
    "Horse" -> "🐴",
    "Moose" -> "🫎",
    "Donkey" -> "🫏",
    "Horse" -> "🐎",
    "Unicorn" -> "🦄",
    "Zebra" -> "🦓",
    "Deer" -> "🦌",
    "Bison" -> "🦬",
    "Cow" -> "🐮",
    "Ox" -> "🐂",
    "Boar" -> "🐗",
    "Ram" -> "🐏",
    "Sheep" -> "🐑",
    "Goat" -> "🐐",
    "Camel" -> "🐪",
    "Llama" -> "🦙",
    "Giraffe" -> "🦒",
    "Elephant" -> "🐘",
    "Mammoth" -> "🦣",
    "Rhinoceros" -> "🦏",
    "Hippopotamus" -> "🦛",
    "Mouse" -> "🐭",
    "Rat" -> "🐀",
    "Hamster" -> "🐹",
    "Rabbit" -> "🐰",
    "Chipmunk" -> "🐿️",
    "Beaver" -> "🦫",
    "Hedgehog" -> "🦔",
    "Bat" -> "🦇",
    "Bear" -> "🐻",
    "Koala" -> "🐨",
    "Panda" -> "🐼",
    "Sloth" -> "🦥",
    "Otter" -> "🦦",
    "Skunk" -> "🦨",
    "Kangaroo" -> "🦘",
    "Badger" -> "🦡",
    "Turkey" -> "🦃",
    "Chicken" -> "🐔",
    "Rooster" -> "🐓",
    "Bird" -> "🐦",
    "Penguin" -> "🐧",
    "Eagle" -> "🦅",
    "Duck" -> "🦆",
    "Swan" -> "🦢",
    "Owl" -> "🦉",
    "Dodo" -> "🦤",
    "Flamingo" -> "🦩",
    "Peacock" -> "🦚",
    "Parrot" -> "🦜",
    "Wing" -> "🪽",
    "Goose" -> "🪿",
    "Crocodile" -> "🐊",
    "Turtle" -> "🐢",
    "Lizard" -> "🦎",
    "Snake" -> "🐍",
    "Dragon" -> "🐉",
    "Dinosaur" -> "🦕",
    "Whale" -> "🐋",
    "Dolphin" -> "🐬",
    "Seal" -> "🦭",
    "Fish" -> "🐟",
    "Blowfish" -> "🐡",
    "Shark" -> "🦈",
    "Octopus" -> "🐙",
    "Coral" -> "🪸",
    "Jellyfish" -> "🪼",
    "Snail" -> "🐌",
    "Butterfly" -> "🦋",
    "Bug" -> "🐛",
    "Ant" -> "🐜",
    "Honeybee" -> "🐝",
    "Beetle" -> "🪲",
    "Cricket" -> "🦗"
  )

  val defaultEmoji: String = "👤"

  def getRandomUsername: String =
    "Anonymous " + Random.nextInt(20)

  def getUsernameEmoji(username: String): String =
    "👤"

  case class HSLColor(hue: Int, saturation: Float, lightness: Float):
    def toCSS = f"hsl($hue, ${saturation * 100}%%, ${lightness * 100}%%)"

  def generateColorForUsername(username: String): HSLColor =
    ???
