package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.elements._
import gui.scenes.panes.vehicle._
import logic.vehicle._
import logic.company._
import formatter._

/** Display a catalog of engines and carriages.
 *
 *  Engines/Carriages can be previewed and bougth from the catalog.
 *  @param company the company buying new stock.
 *  @param updateStock a callback used to indicate that the company stock
 *   needs to be updated.
 */
class Catalog(company: Company) extends VBox(3) {
  // 2 submenus : engines and carriages
  private val typeList: SelectionMenu = new SelectionMenu()
  typeList.addMenu("engines", listEngines)
  typeList.addMenu("carriages", listCarriages)
  typeList.addMenu("trucks", listTrucks)
  typeList.addMenu("planes", listPlanes)
  typeList.addMenu("ships", listShips)

  private val sep1: Separator = new Separator()

  // each submenu has a list of models
  private val enginesList: SelectionList[EngineModel] =
    new SelectionList[EngineModel](
      EngineModel.models.values.toList,
      _.name,
      displayEngine(_))

  private val carriagesList: SelectionList[CarriageModel] =
    new SelectionList[CarriageModel](
      CarriageModel.models.values.toList,
      _.name,
      displayCarriage(_))

  private val trucksList: SelectionList[TruckModel] =
    new SelectionList[TruckModel](
      TruckModel.models.values.toList,
      _.name,
      displayTruck(_))

  private val planesList: SelectionList[PlaneModel] =
    new SelectionList[PlaneModel](
      PlaneModel.models.values.toList,
      _.name,
      displayPlane(_))

  private val shipsList: SelectionList[ShipModel] =
    new SelectionList[ShipModel](
      ShipModel.models.values.toList,
      _.name,
      displayShip(_))


  private val sep2: Separator = new Separator()

  // buy button
  private val buy: Button = new Button()

  children = List(typeList, sep1)

  /** Displays the list of engines. */
  private def listEngines(): Unit = {
    enginesList.deselect()
    children = List(typeList, sep1, enginesList, sep2)
  }

  /** Displays a specific engine model. */
  private def displayEngine(engine: EngineModel): Unit = {
    buy.text = "buy(" + MoneyFormatter.format(engine.price) + ")"
    buy.onAction = (event: ActionEvent) => {
      company.buy(Engine(engine, company))
    }
    buy.disable <== company.money < engine.price
    children = List(
      typeList, sep1, enginesList, sep2, buy,
      new VehicleModelStats(engine))
  }

  /** Displays the list of carriages. */
  private def listCarriages(): Unit = {
    carriagesList.deselect()
    children = List(typeList, sep1, carriagesList, sep2)
  }

  /** Displays a specific carriage model. */
  private def displayCarriage(carriage: CarriageModel): Unit = {
    buy.text = "buy(" + MoneyFormatter.format(carriage.price) + ")"
    buy.onAction = (event: ActionEvent) => {
      company.buy(Carriage(carriage, company))
    }
    buy.disable <== company.money < carriage.price
    children = List(
      typeList, sep1, carriagesList, sep2, buy,
      new VehicleModelStats(carriage))
  }

  private def listTrucks(): Unit = {
    trucksList.deselect()
    children = List(typeList, sep1, trucksList, sep2)
  }

  /** Displays a specific engine model. */
  private def displayTruck(truck: TruckModel): Unit = {
    buy.text = "buy(" + MoneyFormatter.format(truck.price) + ")"
    buy.onAction = (event: ActionEvent) => {
      company.buy(Truck(truck, company))
    }
    buy.disable <== company.money < truck.price
    children = List(
      typeList, sep1, trucksList, sep2, buy,
      new VehicleModelStats(truck))
  }

  private def listPlanes(): Unit = {
    planesList.deselect()
    children = List(typeList, sep1, planesList, sep2)
  }

  /** Displays a specific engine model. */
  private def displayPlane(plane: PlaneModel): Unit = {
    buy.text = "buy(" + MoneyFormatter.format(plane.price) + ")"
    buy.onAction = (event: ActionEvent) => {
      company.buy(Plane(plane, company))
    }
    buy.disable <== company.money < plane.price
    children = List(
      typeList, sep1, planesList, sep2, buy,
      new VehicleModelStats(plane))
  }

  private def listShips(): Unit = {
    shipsList.deselect()
    children = List(typeList, sep1, shipsList, sep2)
  }

  /** Displays a specific engine model. */
  private def displayShip(ship: ShipModel): Unit = {
    buy.text = "buy(" + MoneyFormatter.format(ship.price) + ")"
    buy.onAction = (event: ActionEvent) => {
      company.buy(Ship(ship, company))
    }
    buy.disable <== company.money < ship.price
    children = List(
      typeList, sep1, shipsList, sep2, buy,
      new VehicleModelStats(ship))
  }
}
