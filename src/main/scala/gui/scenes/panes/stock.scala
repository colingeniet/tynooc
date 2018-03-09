package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scala.util.Try
import scalafx.scene.input.MouseEvent

import gui.draw._
import gui.scenes.elements._
import logic.company._
import logic.train._
import logic.world._
import logic.town._
import formatter._

/** Displays a company rolling stock.
 *
 *  @param company the company.
 *  @param statsTrain a callback used to display information on a train.
 *  @param statsEngine a callback used to display information on a engine.
 *  @param statsCarriage a callback used to display information on a carriage.
 */
class CompanyStock(
  company: Company,
  world: World,
  statsTrain: Train => Unit,
  statsEngine: Engine => Unit,
  statsCarriage: Carriage => Unit)
extends DrawableVBox {
  private val menu: SelectionMenu = new SelectionMenu()
  menu.addMenu("trains", displayTrains())
  menu.addMenu("engines", displayEngines())
  menu.addMenu("carriages", displayCarriages())

  private val sep1: Separator = new Separator()
  private val sep2: Separator = new Separator()

  private var list: Node = new Pane()

  children = List(menu, sep1)
  spacing = 3

  /** Displays the trains list. */
  private def displayTrains(): Unit = {
    list = new TrainList(company.trains.toList, detailTrain)
    children = List(menu, sep1, list)

    // reset draw method
    drawCallback = () => ()
  }

  /** Displays the engines list. */
  private def displayEngines(): Unit = {
    list = new EngineList(company.engines.toList, detailEngine)
    children = List(menu, sep1, list)

    // reset draw method
    drawCallback = () => ()
  }

  /** Displays the carriages list. */
  private def displayCarriages(): Unit = {
    list = new CarriageList(company.carriages.toList, detailCarriage)
    children = List(menu, sep1, list)

    // reset draw method
    drawCallback = () => ()
  }

  /** Displays a specific train. */
  private def detailTrain(train: Train): Unit = {
    // display stats in a separate window via callback
    statsTrain(train)

    // create buttons for assemble/disassemble actions
    val disassembleAll: Button = new Button("Disassemble all")
    val disassembleOne: Button = new Button("Disassemble last")
    val addCarriage: Button = new Button("Add carriage")
    val sendTravel: Button = new Button("Travel")
    val nameField: TextField = new TextField() {
      text = train.name
      onAction = (event: ActionEvent) => {
        train.name = text()
        // redraw train list
        displayTrains()
        // display trains
        detailTrain(train)
      }
    }

    disassembleAll.onAction = (event: ActionEvent) => {
      company.disassembleTrain(train)
      displayTrains()
      // display stats for the engine instead,
      // this is mostly to clear the stats screen
      statsEngine(train.engine)
    }

    disassembleOne.onAction = (event: ActionEvent) => {
      company.removeCarriageFromTrain(train)
      detailTrain(train)
    }

    addCarriage.onAction = (event: ActionEvent) => {
      // when pressing the button, display a new carriage list
      val selectionList: CarriageList =
        new CarriageList(
          company.carriagesStoredAt(train.town).toList,
          carriage => {
            // when selecting a carriage, add it to the train
            company.addCarriageToTrain(train, carriage)
            detailTrain(train)
          })
      // display new selection list upon button pressed
      children = List(
        menu,
        sep1,
        list,
        sep2,
        addCarriage,
        disassembleOne,
        disassembleAll,
        sendTravel,
        nameField,
        new Separator(),
        new Label("select carriage"),
        selectionList)
    }

    sendTravel.onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of towns
      val selectionList: SelectionList[Town] = new SelectionList[Town](
        world.townsAccessibleFrom(train.town),
        _.name,
        town => {
          // when selecting a town, travel to it
          company.launchTravel(train, town)
          detailTrain(train)
        })
      // display new selection list upon button pressed
      children = List(
        menu,
        sep1,
        list,
        sep2,
        addCarriage,
        disassembleOne,
        disassembleAll,
        sendTravel,
        nameField,
        new Separator(),
        new Label("select destination"),
        selectionList)
    }

    // disable buttons as needed
    drawCallback = () => {
      addCarriage.disable = train.onRoute
      disassembleOne.disable = train.onRoute || train.carriages.isEmpty
      disassembleAll.disable = train.onRoute
      sendTravel.disable = train.onRoute || train.tooHeavy || train.isDamaged
    }

    children = List(
      menu,
      sep1,
      list,
      sep2,
      addCarriage,
      disassembleOne,
      disassembleAll,
      sendTravel,
      nameField)
  }

  /** Displays a specific engine. */
  private def detailEngine(engine: Engine): Unit = {
    // display stats in a separate window via callback
    statsEngine(engine)
    // create buttons for engine specific actions
    val createButton: Button = new Button("New train")
    createButton.onAction = (event: ActionEvent) => {
      company.createTrainFromEngine(engine)
    }

    val repairButton: Button = new Button("Repair")
    repairButton.onAction = (event: ActionEvent) => {
      company.repair(engine)
    }

    val upgradeButton: Button = new Button("Upgrade")
    upgradeButton.onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of upgrades
      val selectionList: SelectionList[EngineModel] =
        new SelectionList[EngineModel](
        engine.model.upgrades.map(EngineModel(_)),
        model => s"${model.name}(${MoneyFormatter.format(PriceSimulation.upgradePrice(engine, model))})",
        model => {
          // upgrade engine upon selection
          company.upgrade(engine, model)
          // redraw stock menu
          displayEngines()
          detailEngine(engine)
        })

      // display new selection list upon button pressed
      children = List(
        menu,
        sep1,
        list,
        sep2,
        createButton,
        repairButton,
        upgradeButton,
        new Separator(),
        new Label("select upgrade"),
        selectionList)
    }

    children = List(
      menu,
      sep1,
      list,
      sep2,
      createButton,
      repairButton,
      upgradeButton)

    // disable buttons as needed
    drawCallback = () => {
      createButton.disable = engine.isUsed
      repairButton.disable = engine.isUsed
      upgradeButton.disable = engine.isUsed
    }
  }

  /** Displays a specific carriage. */
  private def detailCarriage(carriage: Carriage): Unit = {
    // display stats in a separate window via callback
    statsCarriage(carriage)

    val repairButton: Button = new Button("Repair")
    val upgradeButton: Button = new Button("Upgrade")

    val priceField: TextField = new TextField() {
      text = s"${MoneyFormatter.format(carriage.placePrice)} (place price by distance)."
      onMouseExited = (event: MouseEvent) => {
        text = s"${MoneyFormatter.format(carriage.placePrice)} (place price by distance)."
      }
      onMouseEntered = (event: MouseEvent) => {
        text = carriage.placePrice.toString
      }
      onAction = (event: ActionEvent) => {
        Try(text().toDouble).toOption match {
          case Some(x) => carriage.placePrice = x
          case None    =>
        }
        parent.value.requestFocus()
      }
    }

    repairButton.onAction = (event: ActionEvent) => {
      company.repair(carriage)
    }

    upgradeButton.onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of upgrades
      val selectionList: SelectionList[CarriageModel] =
        new SelectionList[CarriageModel](
          carriage.model.upgrades.map(CarriageModel(_)),
          model => s"${model.name}(${MoneyFormatter.format(PriceSimulation.upgradePrice(carriage, model))})",
          model => {
          // upgrade engine upon selection
          company.upgrade(carriage, model)
          // redraw stock menu
          displayCarriages()
          detailCarriage(carriage)
        })

      // display new selection list upon button pressed
      children = List(
        menu,
        sep1,
        list,
        sep2,
        repairButton,
        upgradeButton,
        priceField,
        new Separator(),
        new Label("select upgrade"),
        selectionList)
    }


    children = List(menu, sep1, list, sep2, repairButton, upgradeButton, priceField)
    // disable buttons as needed
    drawCallback = () => {
      repairButton.disable = carriage.isUsed
      upgradeButton.disable = carriage.isUsed
    }
  }

  // The actual draw function, changed as needed
  private var drawCallback: () => Unit = () => ()

  override def draw(): Unit = {
    drawCallback()
  }
}

/** Displays a list of trains.
 *
 *  @param trains the list to display.
 *  @param detail a callback called whenever a train is selected from the list.
 */
class TrainList(trains: List[Train], detail: Train => Unit)
extends SelectionList[Train](trains, _.name, detail)

/** Displays a list of carriages.
 *
 *  @param carriages the list to display.
 *  @param detail a callback called whenever a carriage is selected from the list.
 */
class CarriageList(carriages: List[Carriage], detail: Carriage => Unit)
extends SelectionList[Carriage](carriages, _.model.name, detail)

/** Displays a list of engines.
 *
 *  @param engines the list to display.
 *  @param detail a callback called whenever an engine is selected from the list.
 */
class EngineList(
  engines: List[Engine],
  detail: Engine => Unit)
extends SelectionList[Engine](engines, _.model.name, detail)
