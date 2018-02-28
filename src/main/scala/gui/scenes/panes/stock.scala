package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.draw._
import gui.scenes.elements._
import logic.player._
import logic.train._
import logic.world._
import logic.town._

/** Displays a player rolling stock.
 *
 *  @param player the player.
 *  @param statsTrain a callback used to display information on a train.
 *  @param statsEngine a callback used to display information on a engine.
 *  @param statsCarriage a callback used to display information on a carriage.
 */
class PlayerStock(
  player: Player,
  world: World,
  statsTrain: Train => Unit,
  statsEngine: Engine => Unit,
  statsCarriage: Carriage => Unit)
extends DrawableVBox {
  private var menu: SelectionMenu = new SelectionMenu()
  menu.addMenu("trains", displayTrains())
  menu.addMenu("engines", displayEngines())
  menu.addMenu("carriages", displayCarriages())

  private var sep1: Separator = new Separator()
  private var sep2: Separator = new Separator()

  private var list: Node = new Pane()

  children = List(menu, sep1)
  spacing = 3

  /** Displays the trains list. */
  private def displayTrains(): Unit = {
    list = new TrainList(player.trains, detailTrain)
    children = List(menu, sep1, list)

    // reset draw method
    drawCallback = () => ()
  }

  /** Displays the engines list. */
  private def displayEngines(): Unit = {
    list = new EngineList(player.engines, detailEngine)
    children = List(menu, sep1, list)

    // reset draw method
    drawCallback = () => ()
  }

  /** Displays the carriages list. */
  private def displayCarriages(): Unit = {
    list = new CarriageList(player.carriages, detailCarriage)
    children = List(menu, sep1, list)

    // reset draw method
    drawCallback = () => ()
  }

  /** Displays a specific train. */
  private def detailTrain(train: Train): Unit = {
    // display stats in a separate window via callback
    statsTrain(train)
    // create buttons for assemble/disassemble actions
    var disassembleAll: Button = new Button("Disassemble all")
    var disassembleOne: Button = new Button("Disassemble last")
    var addCarriage: Button = new Button("Add carriage")
    var sendTravel: Button = new Button("Travel")

    disassembleAll.onAction = (event: ActionEvent) => {
      player.disassembleTrain(train)
      displayTrains()
      // display stats for the engine instead,
      // this is mostly to clear the stats screen
      statsEngine(train.engine)
    }

    disassembleOne.onAction = (event: ActionEvent) => {
      player.removeCarriageFromTrain(train)
      detailTrain(train)
    }

    addCarriage.onAction = (event: ActionEvent) => {
      // when pressing the button, display a new carriage list
      var selectionList: CarriageList =
        new CarriageList(
          player.carriagesStoredAt(train.town),
          carriage => {
            // when selecting a carriage, add it to the train
            player.addCarriageToTrain(train, carriage)
            detailTrain(train)
          })
      // display new selection list
      children = List(
        menu,
        sep1,
        list,
        sep2,
        addCarriage,
        disassembleOne,
        disassembleAll,
        sendTravel,
        new Separator(),
        new Label("select carriage"),
        selectionList)
    }

    sendTravel.onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of towns
      var selectionList: SelectionList[Town] = new SelectionList[Town](
        world.townsAccessibleFrom(train.town),
        _.name,
        town => {
          // when selecting a town, travel to it
          player.launchTravel(train, town)
          detailTrain(train)
        })
      // display new selection list
      children = List(
        menu,
        sep1,
        list,
        sep2,
        addCarriage,
        disassembleOne,
        disassembleAll,
        sendTravel,
        new Separator(),
        new Label("select destination"),
        selectionList)
    }

    // disable buttons as needed
    drawCallback = () => {
      addCarriage.disable = train.onRoute
      disassembleOne.disable = train.onRoute || train.carriages.isEmpty
      disassembleAll.disable = train.onRoute
      sendTravel.disable = train.onRoute
    }

    children = List(
      menu,
      sep1,
      list,
      sep2,
      addCarriage,
      disassembleOne,
      disassembleAll,
      sendTravel)
  }

  /** Displays a specific engine. */
  private def detailEngine(engine: Engine): Unit = {
    // display stats in a separate window via callback
    statsEngine(engine)
    // create buttons for engine specific actions
    var createButton: Button = new Button("New train")
    createButton.onAction = (event: ActionEvent) => {
      val train: Train = player.createTrainFromEngine(engine)
    }
    children = List(menu, sep1, list, sep2, createButton)

    // reset draw function
    drawCallback = () => createButton.disable = engine.isUsed
  }

  /** Displays a specific carriage. */
  private def detailCarriage(carriage: Carriage): Unit = {
    // display stats in a separate window via callback
    statsCarriage(carriage)

    // reset draw method
    drawCallback = () => ()
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
extends SelectionList[Train](trains, _ => "train", detail)

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
