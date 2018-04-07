package logic.facility

import scala.collection.mutable.HashMap
import scalafx.beans.property._

import logic.model._
import logic.good._
import logic.town._
import logic.game._
import logic.company._



class ProductionCycle(
  val consumes: HashMap[Good, Double],
  val produces: HashMap[Good, Double],
  val cycleTime: Double) //In days ?

class FactoryModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val productions: List[ProductionCycle])
extends FacilityModel

object FactoryModel extends ModelNameMap[FactoryModel] {

 private val default_model: FactoryModel = new FactoryModel("Stuff Factory", 42, List(),
   List(new ProductionCycle(Good.any(1), HashMap(Stuff -> 1000), 1)))

 private val _models: HashMap[String, FactoryModel] = new HashMap[String, FactoryModel]()  {
   override def default(name: String) = default_model
 }

 _models += (
  "aluminum_plant" -> new FactoryModel("Aluminium Plant", 1000, List("super_bakery"),
     List(new ProductionCycle(HashMap(Bauxite -> 2, Coal -> 3), HashMap(Aluminium -> 10),5))),
  "bakery" -> new FactoryModel("Bakery", 500, List(),
    List(new ProductionCycle(HashMap(Grain -> 2), HashMap(BakedGoods -> 2), 1))),
  "super_bakery" -> new FactoryModel("Super Bakery", 1200, List(),
    List(new ProductionCycle(HashMap(Grain -> 2), HashMap(BakedGoods -> 2, Chocolate -> 10), 1.2))),
  "bauxite_mine" -> new FactoryModel("Bauxite Mine", 300, List(),
    List(new ProductionCycle(HashMap(), HashMap(Bauxite -> 8), 2))),
  "brewery" -> new FactoryModel("Brewery", 500, List(),
    List(new ProductionCycle(HashMap(Grain -> 2, Glass -> 2), HashMap(Beer-> 3), 2))),
  "brick_works" -> new FactoryModel("Brickworks", 400, List(),
    List(new ProductionCycle(HashMap(Clay -> 4), HashMap(Bricks -> 4), 1))),
  "cannery" -> new FactoryModel("Cannery", 500, List(),
    List(new ProductionCycle(HashMap(Steel -> 2, Fish -> 2), HashMap(CannedFood -> 2), 1),
        new ProductionCycle(HashMap(Steel -> 2, Fruit -> 2), HashMap(CannedFood -> 2), 1),
        new ProductionCycle(HashMap(Steel -> 2, Vegetables -> 2), HashMap(CannedFood -> 2), 1),
        new ProductionCycle(HashMap(Steel -> 2, Meat -> 2), HashMap(CannedFood -> 2), 1))),
  "cattle_ranch" -> new FactoryModel("Cattle Ranch", 500, List(),
    List(new ProductionCycle(HashMap(Grain -> 2), HashMap(Cattle -> 2, Milk -> 4), 7))),
  "cement_factory" -> new FactoryModel("Cement Factory", 800, List(),
    List(new ProductionCycle(HashMap(Coal -> 1, Limestone -> 1), HashMap(Cement -> 1), 1))),
  "chemical_plant" -> new FactoryModel("Chemical Plant", 1000, List(),
    List(new ProductionCycle(HashMap(PetroleumProduct -> 10), HashMap(Chemicals -> 7), 3),
        new ProductionCycle(HashMap(PetroleumProduct -> 10), HashMap(Plastics -> 8), 3))),
  "clay_pit" -> new FactoryModel("Clay Pit", 200, List(),
    List(new ProductionCycle(HashMap(), HashMap(Clay -> 5), 3))),
  "coal_mine" -> new FactoryModel("Coal Mine", 400, List(),
    List(new ProductionCycle(HashMap(), HashMap(Coal-> 8), 4))),
  "copper_mine" -> new FactoryModel("Copper Mine", 700, List(),
    List(new ProductionCycle(HashMap(), HashMap(Copper -> 3), 2))),
  "cotton_plantation" -> new FactoryModel("Cotton Plantation", 500, List(),
    List(new ProductionCycle(HashMap(), HashMap(Cotton -> 14), 8))),
  "electronics_factory" -> new FactoryModel("Electronics Factory", 2000, List(),
    List(new ProductionCycle(HashMap(Glass -> 2, Plastics -> 2, AluminiumWires -> 2), HashMap(Electronics -> 2), 3),
        new ProductionCycle(HashMap(Glass -> 2, Plastics -> 2, CopperWires -> 2), HashMap(Electronics -> 2), 1))),
  "fishery" -> new FactoryModel("Fishery", 650, List(),
    List(new ProductionCycle(HashMap(), HashMap(Fish -> 2), 1))),
  "forestry" -> new FactoryModel("Forestry", 500, List(),
    List(new ProductionCycle(HashMap(), HashMap(Timber -> 1), 0.5))),
  "fruit_orchard" -> new FactoryModel("Fruit Orchard", 1500, List(),
    List(new ProductionCycle(HashMap(), HashMap(Fruit -> 20), 7))),
  "furniture_factory" -> new FactoryModel("Furniture Factory", 3000, List(),
    List(new ProductionCycle(HashMap(Lumber -> 4, Glass -> 4), HashMap(Furniture -> 4), 2),
        new ProductionCycle(HashMap(Lumber -> 4, Leather -> 4), HashMap(Furniture -> 4), 2),
        new ProductionCycle(HashMap(Lumber -> 4, Textiles -> 4), HashMap(Furniture -> 4), 2),
        new ProductionCycle(HashMap(Steel -> 2, Glass -> 4), HashMap(Furniture -> 4), 3),
      new ProductionCycle(HashMap(Steel -> 2, Leather -> 4), HashMap(Furniture -> 4), 3),
    new ProductionCycle(HashMap(Steel -> 2, Textiles -> 4), HashMap(Furniture -> 4), 3))),
  "glass_works" -> new FactoryModel("Glass Works", 800, List(),
    List(new ProductionCycle(HashMap(Coal -> 8, Sand -> 16), HashMap(Glass -> 16), 2))),
  "grain_farm" -> new FactoryModel("Grain Farm", 750, List(),
    List(new ProductionCycle(HashMap(), HashMap(Grain -> 20), 7))),
  "iron_mine" -> new FactoryModel("Iron Mine", 450, List(),
    List(new ProductionCycle(HashMap(), HashMap(Iron -> 0.1), 0.1))),
  "liquor_distillery" -> new FactoryModel("Liquor Distillery", 800, List(),
    List(new ProductionCycle(HashMap(Glass -> 6, Fruit -> 8), HashMap(Liquor -> 7), 0.5),
        new ProductionCycle(HashMap(Glass -> 6, Grain -> 8), HashMap(Liquor -> 7), 0.7))),
  "lumber_mill" -> new FactoryModel("Lumber Mill", 780, List(),
    List(new ProductionCycle(HashMap(Timber -> 5), HashMap(Lumber -> 3, Woodchips -> 2), 0.7))),
  "oil_refinery" -> new FactoryModel("Oil Refinery", 4500, List(),
    List(new ProductionCycle(HashMap(Oil -> 5), HashMap(Fuel -> 5, PetroleumProduct -> 5), 0.6))),
  "oil_wells" -> new FactoryModel("Oil Wells", 1300, List(),
    List(new ProductionCycle(HashMap(), HashMap(Oil -> 10), 1))),
  "paper_mill" -> new FactoryModel("Paper Mill", 700, List(),
    List(new ProductionCycle(HashMap(Woodchips -> 2), HashMap(Paper -> 4), 0.5))),
  "pig_farm" -> new FactoryModel("Pig Farm", 950, List(),
    List(new ProductionCycle(HashMap(Grain -> 18), HashMap(Pigs -> 17), 7),
        new ProductionCycle(HashMap(Vegetables -> 18), HashMap(Pigs -> 17), 5))),
  "printing_press" -> new FactoryModel("Printing Press", 100, List(),
    List(new ProductionCycle(HashMap(Paper -> 2), HashMap(Press -> 1.9), 0.15))),
  "stone_quarry" -> new FactoryModel("Stone Quarry", 444, List(),
    List(new ProductionCycle(HashMap(), HashMap(Limestone -> 5), 2),
        new ProductionCycle(HashMap(), HashMap(Marble -> 5), 2))),
  "rubber_plantation" -> new FactoryModel("Rubber Plantation", 1000, List(),
    List(new ProductionCycle(HashMap(), HashMap(Rubber -> 4), 1))),
  "sand_pit" -> new FactoryModel("Sand Pit", 666, List(),
    List(new ProductionCycle(HashMap(), HashMap(Sand -> 3), 0.1))),
  "sheep_farm" -> new FactoryModel("Sheep Farm", 987, List(),
    List(new ProductionCycle(HashMap(Grain -> 4), HashMap(Sheep -> 2), 3))),
  "steel_mill" -> new FactoryModel("Steel Mill", 857, List(),
    List(new ProductionCycle(HashMap(Iron -> 2, Coal-> 2), HashMap(Steel -> 3), 1.5))),
  "stockyard" -> new FactoryModel("Stockyard", 1200, List(),
    List(new ProductionCycle(HashMap(Cattle -> 4), HashMap(Meat -> 6), 0.7),
        new ProductionCycle(HashMap(Sheep -> 4), HashMap(Meat -> 6), 0.7),
        new ProductionCycle(HashMap(Pigs -> 4), HashMap(Meat -> 6), 0.7))),
  "tannery" -> new FactoryModel("Tannery", 999, List(),
    List(new ProductionCycle(HashMap(Cattle -> 8), HashMap(Leather -> 16), 1))),
  "textile_mill" -> new FactoryModel("Textile Mill", 432, List(),
    List(new ProductionCycle(HashMap(Cotton -> 2), HashMap(Textiles -> 1), 0.05))),
  "tyre_factory" -> new FactoryModel("Tyre Factory", 1230, List(),
    List(new ProductionCycle(HashMap(Chemicals -> 2, Rubber -> 2, SteelWires -> 2), HashMap(Tyres -> 6), 1))),
  "vegetable_farm" -> new FactoryModel("Vegetable Farm", 1100, List(),
    List(new ProductionCycle(HashMap(), HashMap(Vegetables -> 20), 1.7))),
  "vehicle_factory" -> new FactoryModel("Vehicle Factory", 500, List(),
    List(new ProductionCycle(HashMap(Aluminium -> 2, Electronics -> 2, Glass -> 2, Plastics -> 2, Steel -> 2, Tyres -> 2), HashMap(Vehicles -> 10), 5))),
  "vineyard" -> new FactoryModel("Vineyard", 2500, List(),
    List(new ProductionCycle(HashMap(Glass -> 10), HashMap(Wine -> 15), 2))),
  "wire_mill" -> new FactoryModel("Wire Mill", 1280, List(),
    List(new ProductionCycle(HashMap(Aluminium -> 2), HashMap(AluminiumWires -> 3), 3),
        new ProductionCycle(HashMap(Copper -> 2), HashMap(CopperWires -> 2), 2),
        new ProductionCycle(HashMap(Steel -> 2), HashMap(SteelWires -> 1), 0.5)))
  )

 override def models: HashMap[String, FactoryModel] = _models
}

class Factory(model: FactoryModel, _town: Town, _owner: Company)
extends FacilityFromModel[FactoryModel](model, _town, _owner) {
  val working: BooleanProperty = BooleanProperty(false)

  def modelNameMap(name: String): FactoryModel = FactoryModel(name)

  def startCycle(): Unit = {
    if (!working()) {
      model.productions.find(p => town.available(p.consumes)) match {
        case Some(prod) => {
          working() = true
          town.consume(prod.consumes)
          Game.delayAction(prod.cycleTime, () => {
            town.addGoods(prod.produces)
            working() = false
          })
        }
        case None => ()
      }
    }
  }
}
