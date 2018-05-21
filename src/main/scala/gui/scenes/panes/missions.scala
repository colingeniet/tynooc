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
import formatter._


class MissionDetail(mission: Mission) extends VBox(3) {
  children = List(
    new Label(s"from: ${mission.from.name}"),
    new Label(s"to: ${mission.to.name}"),
    new Label(s"deadline: ${TimeFormatter.timeToDateString(mission.time)}"),
    new Label(f"reward: ${MoneyFormatter.format(mission.reward)}"))
}

class HelpMissionDetail(mission: HelpMission) extends MissionDetail(mission) {
  children.addAll(
    new Label {
      text <== createStringBinding(
        () => {
          if(mission.done() == 0) f"${mission.quantity}%.0f ${mission.good.name}"
          else f"${mission.done()}%.0f/${mission.quantity}%.0f ${mission.good.name}"
        },
        mission.done)
    }.delegate)
}

object MissionDetail {
  def apply(mission: Mission): MissionDetail = {
    mission match {
      case m: HelpMission => new HelpMissionDetail(m)
      case _ => new MissionDetail(mission)
    }
  }
}



class CurrentMissions(company: Company)
extends VBox(3) {
  private var list: Node = new SelectionList[Mission](
    company.missions,
    { m: Mission => (s"${m.from.name} -> ${m.to.name}") },
    detailMission(_))

  private val sep: Separator = new Separator()

  children = List(list)

  /** Displays a specific engine. */
  private def detailMission(mission: Mission): Unit = {
    children = List(list, sep, MissionDetail(mission))
  }
}


class NewMissionDetail(
  mission: Mission,
  accept: Mission => Unit,
  decline: Mission => Unit
)
extends VBox(3) {
  children = List(
    MissionDetail(mission),
    new HBox(3) {
      children = List(
        new Button("accept") {
          onAction = (event: ActionEvent) => accept(mission)
        },
        new Button("decline") {
          onAction = (event: ActionEvent) => decline(mission)
        }
      )
    }
  )
}

class NewMissions(company: Company)
extends VBox(3) {
  private var list: Node = new SelectionList[Mission](
    company.waitingMissions,
    { m: Mission => (s"${m.from.name} -> ${m.to.name}") },
    detailMission(_))

  private val sep: Separator = new Separator()

  children = List(list)

  /** Displays a specific engine. */
  private def detailMission(mission: Mission): Unit = {
    children = List(
      list,
      sep,
      new NewMissionDetail(
        mission,
        {m: Mission => {
          company.acceptMission(m)
          children = List(list)
        }},
        { m: Mission => {
          company.rejectMission(m)
          children = List(list)
        }}))
  }
}
