package logic.mission

import scalafx.beans.property._

import logic.town._
import logic.good._
import logic.vehicle._

import java.io._

/*

Multiple types of missions ?

1- Commission,
    WAIT FOR N CARGOS + deliver them to X.

2- Fret
    WAIT FOR full load + deliver them to X.

3- Gathering
    Transport Cargos from X1,...,Xn to Gathering point.

4- Redistribution
    Transport n Cargos from X to X1,...,Xn
*/

abstract class Mission(
  val reward: Double,
  val from: Town,
  val to: Town,
  val time: Double)
extends Serializable {
}


/* The basic mission: you send X ressource to a city */
class HelpMission(reward: Double, from: Town, to: Town, time: Double, val good: Good, val quantity: Double)
extends Mission(reward, from, to, time) {
  @transient var done: DoubleProperty = DoubleProperty(0)

  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.done.toDouble)
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.done = DoubleProperty(stream.readObject().asInstanceOf[Double])
  }
}

/* You wait until full load and then go to a city and then come back to your original city */
/**
  The states gives you carriages with specific capacities
  You stay and fill them, when they're all full you're ready to go.
**/
class FretMission(reward: Double, from: Town, to: Town, time: Double, val carriages : List[Carriage])
extends Mission(reward, from, to, time) {
}

/**

Rapport discussion Yoan:

Les villes génèrent des missions. On voudrait que le joueur a 2 a 3 missions proposées max par jour.
Refuser = no prob. Accepter et echouer = baisse repu.
Reputation influe sur proba d'être choisi pour une mission. 50% Big Brother, 50% players avec reputation (par exemple on prend reputation/(2*sommerepu))...

Quand génère t'on des missions ?

On regarde au début les villes qu'on classe par population. On en en prend 1/7 dans X. Regarde ensuite toute

**/
