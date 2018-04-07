
package logic.good

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._

import logic.vehicle._

import collection.mutable.HashMap

import scala.reflect.ClassTag


trait GoodType {

  def update(g: Good, owner: VehicleUnit, dt : Double) = {}
}

class Solid extends GoodType

class Liquid(val rate: Double) extends GoodType { //Evaporates

  override def update(g: Good, owner: VehicleUnit, dt: Double) = {
    owner.contents(g)() -= rate*dt
  }
}

class Gazeous extends GoodType

class CityNeeded extends GoodType // Is used by cities
class Consumable extends GoodType //Can be consumed
class Perishable() extends GoodType { //Can rot
  /*
  override def update(g: Good, owner: VehicleUnit, dt: Double) = {
    if (owner._owner.trip.time > time) {
      owner.contents(g)() = 0
      Game.printMessage(s"Yuck! Something from ${owner.owner.name} has rotten! It's all gone to waste!")
    }
  }
  */
}

class Dangerous extends GoodType //Passengers won't be happy with  a dangerous good on board
class Expensive extends GoodType //Your vehicle can get attacked by bad people
class Flamable extends GoodType // Can burn
class Alive extends GoodType // Is alive ...
class Fragile extends GoodType // Can easily break if not in an appropriate mean of transport.

class Satisfiable extends GoodType // Can be satisfied or dissatisfied  ...

object Good {

  //Is it possible to store those "dynamically", ie avoid adding Goods here when you add a Good down there...
  val all: List[Good] = List(Passengers, Chocolate, Water, IronOre, Iron, Food, Gaz, Uranium, Stuff,
  Aluminium, AluminiumWires, BakedGoods, Bauxite, Beer, Bricks, CannedFood, Cattle, Cement, Chemicals, Clay,
  Coal, Copper, CopperWires, Cotton, Electronics, Fish,
  Fruit, Fuel, Furniture, Glass, Grain, Iron, Leather, Limestone,
  Liquor, Lumber, Marble , Meat , Milk, Oil, Paper, PetroleumProduct, Pigs, Plastics, Press,
  Rubber, Sand, Sheep, Steel, SteelWires, Textiles, Timber, Tyres, Vegetables,
  Vehicles, Wine, Woodchips, Wool)

  def any(q: Double) : HashMap[Good, Double] = {

    val a: HashMap[Good, Double] = new HashMap()
    all.foreach{ g => a(g) = q }
    a
  }

  def empty: HashMap[Good, DoubleProperty] = {
    val a: HashMap[Good, DoubleProperty] = new HashMap()
    a
  }

  def none: HashMap[Good, Double] = {
    val a: HashMap[Good, Double] = new HashMap()
    a
  }

  def filter[A <: GoodType:ClassTag]: List[Good] = {
    all.filter{g => g.hasProp[A]}
  }

  def anyWith[A <: GoodType:ClassTag](q: Double): HashMap[Good, Double] = {
    val a: HashMap[Good, Double] = new HashMap()
    filter[A].foreach{ g => a(g) = q }
    a
  }

}

class Good(val properties: List[GoodType]) {

  def update(owner: VehicleUnit, dt: Double) : Unit = {

    properties.foreach{ _.update(this, owner, dt) }
  }

  def hasProp[A <: GoodType:ClassTag] : Boolean = {
    properties.foldLeft(false){(b, gtype) => gtype match {
      case _: A => true
      case _ => b
    }}
  }

  def name(): String = getClass.getSimpleName.toLowerCase()

}

//My objects

// Necessary
object Passengers extends Good(List(new Satisfiable(), new Alive()))
object Stuff extends Good(List()) // A good for unknown factories

//For fun
object Chocolate extends Good(List(new Consumable(), new Perishable()))
object Water extends Good(List(new Liquid(0.001), new Consumable()))
object IronOre extends Good(List(new Solid()))
//object Wood extends Good(List(new Solid(), new Flamable()))
object Food extends Good(List(new Solid(), new Consumable(), new Perishable()))
object Gaz extends Good(List(new Gazeous(), new Dangerous()))
//object Coal extends Good(List(new Solid()))
//object Oil extends Good(List(new Liquid(0), new Flamable()))
object Uranium extends Good(List(new Solid(), new Dangerous(), new Expensive()))


//Juraj's
object Aluminium extends Good(List(new Solid(), new CityNeeded()))
object AluminiumWires extends Good(List(new Solid()))
object BakedGoods extends Good(List(new Perishable(), new Consumable(), new CityNeeded()))
object Bauxite extends Good(List(new Solid()))
object Beer extends Good(List(new Liquid(0.001), new Consumable(), new Perishable(), new CityNeeded()))
object Bricks extends Good(List(new Solid()))
object CannedFood extends Good(List(new Consumable(), new Perishable(), new CityNeeded()))
object Cattle extends Good(List(new Alive()))
object Cement extends Good(List(new Liquid(0), new CityNeeded()))
object Chemicals extends Good(List(new Liquid(0), new Dangerous(), new CityNeeded()))
object Clay extends Good(List(new Solid()))
object Coal extends Good(List(new Solid()))
object Copper extends Good(List(new Solid()))
object CopperWires extends Good(List(new Solid()))
object Cotton extends Good(List(new Flamable()))
object Electronics extends Good(List(new Solid(), new CityNeeded()))
object Fish extends Good(List(new Consumable(), new Perishable(), new CityNeeded()))
object Fruit extends Good(List(new Consumable(), new Perishable(), new CityNeeded()))
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
object Meat extends Good(List(new Solid(), new Consumable(), new Perishable(), new CityNeeded()))
object Milk extends Good(List(new Liquid(0.001), new Consumable(), new Perishable(), new CityNeeded()))
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
object Vegetables extends Good(List(new CityNeeded(), new Perishable(), new Consumable()))
object Vehicles extends Good(List(new Solid(), new CityNeeded()))
object Wine extends Good(List(new Liquid(0), new Perishable(), new CityNeeded()))
object Woodchips extends Good(List(new Flamable(), new Solid()))
object Wool extends Good(List(new Flamable()))
