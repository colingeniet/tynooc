package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.panes.model._
import gui.scenes.elements._
import logic.mission._
import logic.company._


class MissionDetail(mission: Mission)
extends Stats(mission)

class CurrentMissions(company: Company)
extends VBox(3) {
  private var list: Node = new SelectionList[Mission](
    company.missions,
    { m: Mission => (s"${m.from} -> ${m.to}") },
    detailMission(_))

  private val sep: Separator = new Separator()

  children = List(list)

  /** Displays a specific engine. */
  private def detailMission(mission: Mission): Unit = {
    children = List(list, sep, new MissionDetail(mission))
  }
}


class NewMissionDetail(mission: Mission)
extends VBox(3) {
  children = List(
    new MissionDetail(mission),
    new Button("accept") {
      onAction = (event: ActionEvent) => () // accept or whatever
    }
  )
}

class NewMissions(world: World)
extends VBox(3) {
  private var list: Node = new SelectionList[Mission](
    List(), // TODO world.missions,
    { m: Mission => (s"${m.from} -> ${m.to}") },
    detailMission(_))

  private val sep: Separator = new Separator()

  children = List(list)

  /** Displays a specific engine. */
  private def detailMission(mission: Mission): Unit = {
    children = List(list, sep, new NewMissionDetail(mission))
  }
}
