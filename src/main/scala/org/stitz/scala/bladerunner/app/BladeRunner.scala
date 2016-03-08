package org.stitz.scala.bladerunner.app

import java.nio.file.Paths
import org.stitz.scala.bladerunner.file.DirectoryActor
import org.stitz.scala.bladerunner.file.DirectoryResult
import org.stitz.scala.bladerunner.file.Process
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import scala.concurrent.duration._
import scalafx.Includes.eventClosureWrapperWithParam
import scalafx.Includes.jfxActionEvent2sfx
import scalafx.Includes.observableList2ObservableBuffer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.event.ActionEvent
import scalafx.geometry.HPos
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.control.Button.sfxButton2jfx
import scalafx.scene.control.Label
import scalafx.scene.control.Label.sfxLabel2jfx
import scalafx.scene.control.TextField
import scalafx.scene.control.Tooltip.stringToTooltip
import scalafx.scene.layout.ColumnConstraints
import scalafx.scene.layout.ColumnConstraints.sfxColumnConstraints2jfx
import scalafx.scene.layout.GridPane
import scalafx.scene.layout.HBox
import scalafx.scene.layout.HBox.sfxHBox2jfx
import scalafx.scene.layout.Priority
import scalafx.scene.layout.VBox
import scalafx.stage.DirectoryChooser
import akka.util.Timeout
import org.stitz.scala.bladerunner.file.DirectoryResult

object BladeRunner extends JFXApp {

  stage = new PrimaryStage {
    title = "Blade Runner - eliminate replicas"
    scene = new Scene(516, 387) {
      content = new VBox {
        vgrow = Priority.Always
        hgrow = Priority.Always

        children = List(new GridPane {
          padding = Insets(25)
          gridLinesVisible = true

          val label = new Label("Directory:")
          GridPane.setHalignment(label, HPos.Right)
          GridPane.setConstraints(label, 0, 0)
          val all = new ColumnConstraints { percentWidth = 100 }

          columnConstraints ++= Seq(new ColumnConstraints(label.prefWidth.value),
            all)

          val selectFile = new Button {
            text = "..."
            onAction = (e: ActionEvent) => {
              val fileChooser = new DirectoryChooser() {
                title = "Pick a directory to analyse"
              }

              val root = fileChooser.showDialog(stage).getAbsolutePath
              fileField.text.update(root)
            }
            tooltip = "Select file..."
          }
          val fileField = new TextField
          val combinedField = new HBox {
            vgrow = Priority.Always
            hgrow = Priority.Always
            children = Seq(fileField, selectFile)
          }
          GridPane.setMargin(combinedField, Insets(10, 10, 10, 10))
          GridPane.setConstraints(combinedField, 1, 0)

          val startAnalysis = new Button {
            text = "Start analysis!"
            onAction = (e: ActionEvent) => {
              val system = ActorSystem("BladeRunner")
              val root = fileField.text.value
              if (!root.isEmpty()) {
                val actor = system.actorOf(Props(new DirectoryActor()),
                  name = "root")
                implicit val timeout = Timeout(25 seconds)
                import system.dispatcher
                val future = actor ? Process(Paths.get(root))
                future.map {
                  case DirectoryResult(totalFileCount) =>
                    println("\n\tFiles found: \t\t%s".format(totalFileCount))
                    system.terminate()
                }
              }
            }
          }
          GridPane.setConstraints(startAnalysis, 1, 1)

          children ++= Seq(
            label,
            combinedField,
            startAnalysis)
        })

      }

    }
  }

}