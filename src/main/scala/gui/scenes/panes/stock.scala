package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.elements._
import logic.player._
import logic.train._

// REMOVE
import logic.game._

/** Displays a player rolling stock.
 *
 *  @param player the player.
 *  @param statsTrain a callback used to display information on a train.
 *  @param statsEngine a callback used to display information on a engine.
 *  @param statsCarriage a callback used to display information on a carriage.
 */
class PlayerStock(
  player: Player,
  statsTrain: Train => Unit,
  statsEngine: Engine => Unit,
  statsCarriage: Carriage => Unit)
extends VBox(3) {
  private var menu: SelectionMenu = new SelectionMenu()
  menu.addMenu("trains", displayTrains())
  menu.addMenu("engines", displayEngines())
  menu.addMenu("carriages", displayCarriages())

  private var sep1: Separator = new Separator()
  private var sep2: Separator = new Separator()

  private var list: Node = new Pane()

  children = List(menu, sep1)

  /** Displays the trains list. */
  private def displayTrains(): Unit = {
    list = new TrainList(player.trains, detailTrain)
    children = List(menu, sep1, list)
  }

  /** Displays the engines list. */
  private def displayEngines(): Unit = {
    list = new EngineList(player.engines, detailEngine)
    children = List(menu, sep1, list)
  }

  /** Displays the carriages list. */
  private def displayCarriages(): Unit = {
    list = new CarriageList(player.carriages, statsCarriage)
    children = List(menu, sep1, list)
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
    if(train.carriages.isEmpty) disassembleOne.disable = true

    addCarriage.onAction = (event: ActionEvent) => {
      // when pressing the button, display a new carriage list
      var selectionList: CarriageList =
        new CarriageList(
          player.carriages,
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
      player.launchTravel(train, Game.world.towns(1)) // nothing to see here, move along
      detailTrain(train)
    }
    if(train.onRoute) {
      addCarriage.disable = true
      disassembleOne.disable = true
      disassembleAll.disable = true
      sendTravel.disable = true
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
      displayEngines()
      // display stats for the train instead,
      // this is mostly to clear the stats screen
      statsTrain(train)
    }
    children = List(menu, sep1, list, sep2, createButton)
  }
}

/** Displays a list of trains.
 *
 *  @param trains the list to display.
 *  @param detail a callback called whenever a train is selected from the list.
 */
class TrainList(trains: List[Train], detail: Train => Unit)
extends ScrollPane {
  private var list: SelectionMenu = new SelectionMenu()
  trains.foreach(train => list.addMenu("train", detail(train))) // needs name

  content = list
}

/** Displays a list of carriages.
 *
 *  @param carriages the list to display.
 *  @param detail a callback called whenever a carriage is selected from the list.
 */
class CarriageList(carriages: List[Carriage], detail: Carriage => Unit)
extends ScrollPane {
  private var list: SelectionMenu = new SelectionMenu()
  carriages.foreach(carriage =>
    list.addMenu(carriage.model.name, detail(carriage)))

  content = list
}

/** Displays a list of engines.
 *
 *  @param engines the list to display.
 *  @param detail a callback called whenever an engine is selected from the list.
 */
class EngineList(
  engines: List[Engine],
  detail: Engine => Unit)
extends ScrollPane {
  private var list: SelectionMenu = new SelectionMenu()
  engines.foreach(engine => list.addMenu(engine.model.name, detail(engine)))

  content = list
}
