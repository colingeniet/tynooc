/* Bottom pane : town/route display */

package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.geometry._

import gui.scenes.elements._
import gui.scenes.panes.model._
import gui.scenes.panes.facility._
import formatter._
import logic.town._
import logic.route._
import logic.facility._
import logic.company._
import logic.good._


/** Town display panel.
 *
 *  @param town the town to display.
 *  @param displayRoute callback used to display a route.
 */
class TownInfo(
  town: Town,
  company: Company,
  displayRoute: Route => Unit,
  displayFacility: Facility => Unit)
extends VBox(3) {
  private val popLbl = new Label {
    text <== createStringBinding(
      () => s"Population: ${town.population.toInt}",
      town.population)
  }
  private val pasLbl = new Label {
    text <== createStringBinding(
      () => s"Passengers: ${town.passengersNumber.toInt}",
      town.passengersNumber)
  }

  private val routes = new VBox {
    children = new Label("Routes to:") :: town.routes.map{ route => new Link(
        f"${route.end.name} - ${route.length}%.0f (${route.name})",
        () => displayRoute(route))
    }
  }

  private val facilities = new SelectionList[Facility](
    town.facilities,
    _.model.name,
    detailFacility(_))

  private val buyFacility = new Button("new factory") {
    onAction = (event: ActionEvent) => {
      val selectionList = new SelectionList[FactoryModel](
        FactoryModel.models.values.toList,
        m => s"${m.name}(${MoneyFormatter.format(m.price)})",
        model => {
          if (company.money() >= model.price) {
            val f: Factory = new Factory(model, town, company)
            company.buy(f)
            town.facilities.add(f)
            setChildren()
          }
        })

      setChildren()
      children.add(selectionList)
    }
  }

  private val buyStation = new Button("new station") {
    onAction = (event: ActionEvent) => {
      val selectionList = new SelectionList[StationModel](
        TrainStationModel.models.values.toList :::
        PortModel.models.values.toList :::
        AirportModel.models.values.toList,
        m => s"${m.name}(${MoneyFormatter.format(m.price)})",
        model => {
          if (company.money() >= model.price) {
            val s: Station = model match {
              case m: TrainStationModel => new TrainStation(m, town, company)
              case m: PortModel => new Port(m, town, company)
              case m: AirportModel => new Airport(m, town, company)
            }
            company.buy(s)
            town.facilities.add(s)
            setChildren()
          }
        })

      setChildren()
      children.add(selectionList)
    }
  }


  private def setChildren(): Unit = {
    children = List(
      new Label(town.name),
      popLbl,
      pasLbl,
      routes,
      facilities,
      buyFacility,
      buyStation)
  }

  private def detailFacility(facility: Facility): Unit = {
    setChildren()
    children.add(new Separator())
    children.add(FacilityMenu(facility, company))
    displayFacility(facility)
  }

  setChildren()
}

/** Town display panel.
 *
 *  @param route the route to display.
 *  @param displayTown callback used to display a town.
 */
class RouteInfo(route: Route, displayTown: Town => Unit)
extends VBox(3) {
  children = List(
    new Label(route.name),
    new Link(route.start.name, () => displayTown(route.start)),
    new Link(route.end.name, () => displayTown(route.end)),
    new Label(f"length: ${route.length}%.0f"),
    new Stats(route) {
      override def filter(a: java.lang.reflect.Field): Boolean = {
        a.getName() == "start" || a.getName() == "end" ||
          a.getName() == "length" || super.filter(a)
      }
    })
}


class TownStock(town: Town) extends ScrollPane {
  content = new VBox {
    children = Good.all.map(g => {
      new Label {
        text <== createStringBinding(
          () => f"${g.name}: ${town.goods(g)()}%.1f (${MoneyFormatter.format(town.goods_prices(g)())})",
          town.goods(g),
          town.goods_prices(g))
      }
    })
  }
}
