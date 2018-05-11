
package logic.good

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._

import logic.vehicle._
import logic.room._
import utils._

import collection.mutable.HashMap

import scala.reflect.ClassTag

/** A base class for all good types. Used to tidy up code
*/
@SerialVersionUID(0L)
trait GoodType extends Serializable {
  def update(g: Good, owner: Room, dt : Double): Unit = ()
}

class Solid extends GoodType

class Liquid(val rate: Double) extends GoodType { //Evaporates

  /** Make the liquid evaporate over time
  * @param g The good who is evaporating (it thus have a liquid property)
  * @param room The room the good is in
  * @param dt Time since last update
  */
  override def update(g: Good, room: Room, dt: Double) = {
    room.contents.values.foreach(h => {
      h(g) -= rate*dt*h(g)
      room.travel.contents(g)() -= rate*dt*h(g)
    })
  }
}

class Gazeous extends GoodType

class CityNeeded extends GoodType // Is used by cities
class Consumable extends GoodType //Can be consumed
class Perishable(val rate: Double) extends GoodType { //Can rot, same as Liquid for now

  /** Make the persihable rot over time
  * @param g The good who is rotting (it thus have a persihable property)
  * @param room The room the good is in
  * @param dt Time since last update
  */
  override def update(g: Good, room: Room, dt: Double) = {
    room.contents.values.foreach(h => {
      h(g) -= rate*dt*h(g)
      room.travel.contents(g)() -= rate*dt*h(g)
    })
  }
}

/*
The description are for future purposes, they are not used this way now !
*/
class Dangerous extends GoodType //Passengers won't be happy with  a dangerous good on board
class Expensive extends GoodType //Your vehicle can get attacked by bad people
class Flamable extends GoodType // Can burn
class Alive extends GoodType // Is alive ...
class Fragile extends GoodType // Can easily break if not in an appropriate mean of transport.
class Elec extends GoodType

class Satisfiable extends GoodType // Can be satisfied or dissatisfied  ...

object Good {

  //Is it possible to store those "dynamically", ie avoid adding Goods here when you add a Good down there...
  val all: List[Good] = List(Electricity, Chocolate, Water, Gaz, Uranium, Stuff,
  Aluminium, AluminiumWires, BakedGoods, Bauxite, Beer, Bricks, CannedFood, Cattle, Cement, Chemicals, Clay,
  Coal, Copper, CopperWires, Cotton, Electronics, Fish,
  Fruit, Fuel, Furniture, Glass, PhilosophalStone, Grain, Iron, Leather, Limestone,
  Liquor, Lumber, Marble , Meat , Milk, Oil, Paper, PetroleumProduct, Pigs, Plastics, Press,
  Rubber, Sand, Sheep, Steel, SteelWires, Textiles, Timber, Tyres, Vegetables,
  Vehicles, Wine, Woodchips, Wool)

  /** Returns a HashMap making every good available
  * @param q the quantity associated to any Good
  */
  def any(q: Double) : HashMap[Good, Double] = {

    val a: HashMap[Good, Double] = InitHashMap[Good, Double](_ => 0)
    all.foreach{ g => a(g) = q }
    a
  }

  /** Returns a empty HashMap for containers
  */
  def empty: HashMap[Good, DoubleProperty] = {
    val a: HashMap[Good, DoubleProperty] = InitHashMap[Good, DoubleProperty](_ => DoubleProperty(0))
    a
  }

  /** Returns a empty HashMap for allowance
  */
  def none: HashMap[Good, Double] = {
    val a: HashMap[Good, Double] = InitHashMap[Good, Double](_ => 0)
    a
  }

  /** Returns a list of good having a certain property
  * @param A The type of the property you want your goods to have.
  * Might and can easily be improved with sort of "predicates"
  */
  def filter[A <: GoodType:ClassTag]: List[Good] = {
    all.filter{g => g.hasProp[A]}
  }

  /** Returns a HashMap of allowance of a certain quantity
  * @param A the type to be allowed
  * @param q the quantity associated to the type
  */
  def anyWith[A <: GoodType:ClassTag](q: Double): HashMap[Good, Double] = {
    val a: HashMap[Good, Double] = InitHashMap[Good, Double](_ => 0)
    filter[A].foreach{ g => a(g) = q }
    a
  }

  /** Modify the allowance of a certain type of a given HashMap
  * @param A The type that you want to set the quantity to
  * @param a The hashmap to set the quantity to
  * @param q The quantity to set
  */
  def setAnyWith[A <: GoodType:ClassTag](a: HashMap[Good, Double], q: Double): Unit = {
    filter[A].foreach{ g => a(g) = q }
  }

}

/** Represents an instance of a Good
* It is not the same thing as a real good in the game. Goods aren't treated individually in the game.
* This represents a certain good such as Water, etc ...
* @param properties The properties that you want your good to have, ie Liquid, Solid, Perishable etc...
*/
@SerialVersionUID(0L)
class Good(val properties: List[GoodType]) extends Serializable {

  def basePrice: Double = 1

  /** Updates a good according to it's properties
  * @param room The room in what a group of this good is stored.
  * @param dt The time since last update
  */
  def update(room: Room, dt: Double) : Unit = {
    properties.foreach{ _.update(this, room, dt) }
  }

  /** Check if a good has a certain property
  * @param A The type you want to check if your good has
  */
  def hasProp[A <: GoodType:ClassTag] : Boolean = {
    properties.foldLeft(false){(b, gtype) => gtype match {
      case _: A => true
      case _ => b
    }}
  }

  def name: String = getClass.getSimpleName.toLowerCase().replace("$", "")
}

//My objects
// Necessary for programming purposes
object Stuff extends Good(List()) // A good for unknown factories

//For fun
object Chocolate extends Good(List(new Consumable(), new Perishable(0.01), new CityNeeded())) //Everyone loves chocolate
object Water extends Good(List(new Liquid(0.001), new Consumable(), new CityNeeded())) //Water is cheap to produce and necessary
//object IronOre extends Good(List(new Solid()))
//object Wood extends Good(List(new Solid(), new Flamable()))
//object Food extends Good(List(new Solid(), new Consumable(), new Perishable()))
object Gaz extends Good(List(new Gazeous(), new Dangerous(), new CityNeeded())) // Same as water
//object Coal extends Good(List(new Solid()))
//object Oil extends Good(List(new Liquid(0), new Flamable()))
object Uranium extends Good(List(new Solid(), new Dangerous(), new Expensive())) //This allows to produce electricity cheaply without a furnisher but is hard to produce (except if you find certain easter eggs)
object Electricity extends Good(List(new CityNeeded(), new Elec())) //Really necessary to produce, and none of the cities have a furnisher at the beginning ! Easy money
object PhilosophalStone extends Good(List(new Solid(), new Expensive())) //One of the easter egg mentioned above

//Juraj's
object Aluminium extends Good(List(new Solid(), new CityNeeded()))
object AluminiumWires extends Good(List(new Solid()))
object BakedGoods extends Good(List(new Perishable(0.01), new Consumable(), new CityNeeded()))
object Bauxite extends Good(List(new Solid()))
object Beer extends Good(List(new Liquid(0.001), new Consumable(), new Perishable(0.01), new CityNeeded()))
object Bricks extends Good(List(new Solid()))
object CannedFood extends Good(List(new Consumable(), new Perishable(0.01), new CityNeeded()))
object Cattle extends Good(List(new Alive()))
object Cement extends Good(List(new Liquid(0), new CityNeeded()))
object Chemicals extends Good(List(new Liquid(0), new Dangerous(), new CityNeeded()))
object Clay extends Good(List(new Solid()))
object Coal extends Good(List(new Solid()))
object Copper extends Good(List(new Solid()))
object CopperWires extends Good(List(new Solid()))
object Cotton extends Good(List(new Flamable()))
object Electronics extends Good(List(new Solid(), new CityNeeded()))
object Fish extends Good(List(new Consumable(), new Perishable(0.01), new CityNeeded()))
object Fruit extends Good(List(new Consumable(), new Perishable(0.01), new CityNeeded()))
object Fuel extends Good(List(new CityNeeded(), new Expensive(), new Liquid(0)))
object Furniture extends Good(List(new Solid(), new CityNeeded()))
object Glass extends Good(List(new Solid(), new Fragile(), new CityNeeded()))
object Grain extends Good(List(new Solid(), new CityNeeded()))
object Iron extends Good(List(new Solid(), new CityNeeded(), new Expensive()))
object Leather extends Good(List(new Solid(), new CityNeeded()))
object Limestone extends Good(List(new Solid()))
object Liquor extends Good(List(new Liquid(0.001), new CityNeeded()))
object Lumber extends Good(List(new Solid(), new Flamable()))
object Marble extends Good(List(new Solid(), new CityNeeded(), new Expensive()))
object Meat extends Good(List(new Solid(), new Consumable(), new Perishable(0.01), new CityNeeded()))
object Milk extends Good(List(new Liquid(0.001), new Consumable(), new Perishable(0.01), new CityNeeded()))
object Oil extends Good(List(new Liquid(0), new Flamable(), new Expensive()))
object Paper extends Good(List(new Solid(), new Flamable(), new CityNeeded()))
object PetroleumProduct extends Good(List(new Liquid(0), new Flamable(), new Expensive()))
object Pigs extends Good(List(new Alive()))
object Plastics extends Good(List(new Solid(), new CityNeeded()))
object Press extends Good(List(new Solid(), new CityNeeded()))
object Rubber extends Good(List(new Solid(), new CityNeeded()))
object Sand extends Good(List(new Solid()))
object Sheep extends Good(List(new Alive()))
object Steel extends Good(List(new Solid(), new Expensive(), new CityNeeded()))
object SteelWires extends Good(List(new Solid()))
object Textiles extends Good(List(new Flamable(), new CityNeeded()))
object Timber extends Good(List(new Flamable(), new CityNeeded()))
object Tyres extends Good(List(new Solid(), new CityNeeded()))
object Vegetables extends Good(List(new CityNeeded(), new Perishable(0.01), new Consumable()))
object Vehicles extends Good(List(new Solid(), new CityNeeded()))
object Wine extends Good(List(new Liquid(0), new Perishable(0.0001), new CityNeeded()))
object Woodchips extends Good(List(new Flamable(), new Solid()))
object Wool extends Good(List(new Flamable()))
