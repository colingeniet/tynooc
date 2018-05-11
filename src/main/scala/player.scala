package player

import logic.company._

/** A player of the game.
  *
  * @constructor Creates a player with a company.
  * @param company The company of the player.
  */
@SerialVersionUID(0L)
class Player(val company : Company)
extends Serializable
