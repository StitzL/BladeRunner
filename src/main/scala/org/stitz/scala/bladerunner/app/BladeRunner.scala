package org.stitz.scala.bladerunner.app

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

object BladeRunner extends JFXApp {
  
  stage = new PrimaryStage {
    title = "Blade Runner - eliminate replicas"
    scene = new Scene(500, 130) {
      root = new HBox {
        val content = new GridPane {
          padding = Insets(25)
          gridLinesVisible = true

          val fileField = new TextField
          val selectFile = new Button("...") {
            onAction = (e: ActionEvent) => mainController.selectFile(fileField.text)
            tooltip = "Select file..."
          }
          HBox.setHgrow(selectFile, Priority.Always)
          HBox.setHgrow(fileField, Priority.Always)

          // @formatter:off

          children = new GridPaneContentBuilder()
            .add(new Label("Directory:"))
            .setHalignment(HPos.Right)
            .setConstraints(0, 0)
            .add(new HBox (fileField, selectFile))
            .setMargin(Insets(10, 10, 10, 10))
            .setConstraints(1, 0)
            .setHgrow(Priority.Always)
            .add(new HBox(10,
                new Button("Start analysis!") {
                  onAction = (e: ActionEvent) => mainController.startAnalysis(fileField.text.value)
                },
                new Button("Abort!") {
                  onAction = (e: ActionEvent) => mainController.stopAnalysis
                }
            ))
            .setMargin(Insets(10, 10, 10, 10))
            .setConstraints(1, 1)
            .build

          // @formatter:on
        }
        HBox.setHgrow(content, Priority.Always)
        children = List(content)
      }

    }
  }
  
  val mainController = new MainController(stage)
}
