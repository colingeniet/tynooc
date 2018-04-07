
package logic.good

import logic.vehicle._

import collection.mutable.HashMap

import scala.reflect.ClassTag

trait GoodType {

  def update(g: Good, owner: VehicleUnit, dt : Double) = {}
}

class Solid extends GoodType

class Liquid(val rate: Double) extends GoodType { //Evaporates

  override def update(g: Good, owner: VehicleUnit, dt: Double) = {
    owner.contents(g) -= rate*dt
  }
}

class Gazeous extends GoodType

class Consumable extends GoodType //Can be consumed
class Perishable extends GoodType //Can rot
class Dangerous extends GoodType //Passengers won't be happy with  a dangerous good on board
class Expensive extends GoodType //Your vehicle can get attacked by bad people
class Flamable extends GoodType

class Satisfiable extends GoodType // Can be satisfied or dissatisfied  ...

object Good {

  //Is it possible to store those "dynamically", ie avoid adding Goods here when you add a Good down there...
  val all: List[Good] = List(Passengers, Chocolate, Water, IronOre, Iron, Wood, Food, Gaz, Coal, Oil, Uranium, Stuff)

  def any(q: Double) : HashMap[Good, Double] = {

    val a: HashMap[Good, Double] = new HashMap()
    all.foreach{ g => a(g) = q }
    return a
  }

  def none: HashMap[Good, Double] = {
    val a: HashMap[Good, Double] = new HashMap()
    return a
  }

}

class Good(val properties: List[GoodType]) {

  def update(owner: VehicleUnit, dt: Double) : Unit = {

    properties.foreach{ _.update(this, owner, dt) }
  }

  def hasProp[A <: GoodType:ClassTag] : Boolean = {
    properties.foldLeft(false){(b, gtype) => gtype match { case _: A => true case _ => b} }
  }

  def name(): String = getClass.getSimpleName.toLowerCase()

}

object Passengers extends Good(List(new Satisfiable()))
object Chocolate extends Good(List(new Consumable(), new Perishable()))
object Water extends Good(List(new Liquid(0.001), new Consumable()))
object IronOre extends Good(List(new Solid()))
object Iron extends Good(List(new Solid(), new Expensive()))
object Wood extends Good(List(new Solid(), new Flamable()))
object Food extends Good(List(new Solid(), new Consumable(), new Perishable()))
object Gaz extends Good(List(new Gazeous(), new Dangerous()))
object Coal extends Good(List(new Solid()))
object Oil extends Good(List(new Liquid(0), new Flamable()))
object Uranium extends Good(List(new Solid(), new Dangerous(), new Expensive()))
object Stuff extends Good(List()) // A good for unknown factories
