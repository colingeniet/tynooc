import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{Stops, LinearGradient}
import scalafx.scene.text.Text

/**

On a besoin d'une finite state machine pour gérer le menu non ?
Chaque bouton doit mener a un état de la state machine


**/

/** Here we build the finite state machine;
      
      Lets say it's
      
      main_menu -> option
      main_menu -> game
      option -> menu
      game -> game_menu
      game_menu -> game
      game_menu -> main_menu
      
      */
      
  

/*Plus tard; menu = new StateMenu() ...*/
val menu = new StateScene() {
      /**
      
      A DECOUPLER, VOIR AU DESSUS
      
      **/
      
      p = Game.fsm
    
      fill = Black
      
      content = new HBox {
        padding = Insets(20)
        children = Seq(
        
          new Text
          {
            text = "Tynooc"
            
            style = "-fx-font-size: 48pt"
            
            fill = new LinearGradient(
              endX = 0,
              stops = Stops(Cyan, DodgerBlue)
            )
            
            effect = new DropShadow {
              color = DodgerBlue
              radius = 25
              spread = 0.25
            }
          },
          
          new Button {
            text = "Play"
            onAction = (event: ActionEvent) =>  { )}
          },
          
          new Button {
          
            text = "Quit"
            onAction = (event: ActionEvent) =>  { stage.close() }
          }
        
        
        )
      }
    }
    
Game = new Game() /** The fsm is in that **/

object ScalaFXHelloWorld extends JFXApp {

  stage = new PrimaryStage {
  
    title = "Tiny Tycoon Tynooc"
    
    state = new FSMScene(Menu.menu)
    StateScene inherit Scene
    scene = state.current //Faux
      
    }
  }
}
