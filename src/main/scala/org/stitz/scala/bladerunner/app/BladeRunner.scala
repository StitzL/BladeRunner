package org.stitz.scala.bladerunner.app

import javafx.event.EventHandler
import javafx.scene.layout.{Background, BackgroundFill, CornerRadii}
import javafx.scene.paint.{CycleMethod, LinearGradient, Stop}
import org.stitz.scala.bladerunner.file.Supervisor
import scalafx.Includes.{eventClosureWrapperWithParam, jfxActionEvent2sfx}
import scalafx.animation.{KeyFrame, Timeline}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.property.{ReadOnlyLongWrapper, ReadOnlyStringWrapper}
import scalafx.beans.value.ObservableValue
import scalafx.event.ActionEvent
import scalafx.geometry.{HPos, Insets, Orientation}
import scalafx.scene.Scene
import scalafx.scene.control.Button.sfxButton2jfx
import scalafx.scene.control.Tooltip.stringToTooltip
import scalafx.scene.control.TreeTableColumn.sfxTreeTableColumn2jfx
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.util.Duration

import scala.collection.JavaConverters._

object BladeRunner extends JFXApp {

  stage = new PrimaryStage {
    title = "Blade Runner - eliminate replicas"
    scene = new Scene(850, 600) {
      root = new VBox {
        val result = new TreeTableView[FileResultBean]() {
          columns ++= Seq(
            new TreeTableColumn[FileResultBean, String]("Path") {
              prefWidth = 400
              cellValueFactory = { p => ReadOnlyStringWrapper(p.getValue().getValue().path()) }
            },
            new TreeTableColumn[FileResultBean, Long]("Size") {
              prefWidth = 100
              cellValueFactory = { p => ReadOnlyLongWrapper(p.getValue().getValue().size()).asInstanceOf[ObservableValue[Long, Long]] }
              cellFactory = { _ =>
                new TreeTableCell(new javafx.scene.control.TreeTableCell[FileResultBean, Long] {

                  override def updateItem(item: Long, empty: Boolean) = {
                    super.updateItem(item, empty)
                    if (!empty && item > 0) {
                      Platform.runLater {
                        if (getTreeTableRow() != null && getTreeTableRow().getTreeItem() != null) {
                          val bean = getTreeTableRow().getTreeItem().getValue()
                          val x = bean.percentDone()
                          val stops = Array(new Stop(0, Color.Green), new Stop(x, Color.White))
                          setBackground(new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops.toList.asJava), CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY)))
                          setText(bean.size().toString())
                        }
                      }
                    }
                  }
                })
              }
            },
            new TreeTableColumn[FileResultBean, String]("Last modified") {
              prefWidth = 130
              cellValueFactory = { p => ReadOnlyStringWrapper(p.getValue().getValue().lastModified()) }
            },
            new TreeTableColumn[FileResultBean, String]("Hash") {
              prefWidth = 200
              cellValueFactory = { p => ReadOnlyStringWrapper(p.getValue().getValue().hash()) }
            })
          tableMenuButtonVisible = true
        }
        val refresher = Timeline(Seq(KeyFrame(Duration(1000), onFinished = (e: ActionEvent) => result.refresh())))
        refresher.setCycleCount(Timeline.Indefinite)
        

        javafx.scene.layout.VBox.setVgrow(result, Priority.Always)

        val content = new GridPane {
          padding = Insets(25)

          val fileField = new TextField
          val selectFile = new Button("...") {
            onAction = (e: ActionEvent) => mainController.selectFile(fileField.text)
            tooltip = "Select file..."
          }
          HBox.setHgrow(fileField, Priority.Always)

          val progressBar = new ProgressBar {
            visible.bind(Supervisor.isRunning)
            prefWidth.bind(fileField.width)
          }
          HBox.setHgrow(progressBar, Priority.Always)

          // @formatter:off
          children = new GridPaneContentBuilder()
            .add(new Label("Directory:") { minWidth = 70 })
            .setHalignment(HPos.Right)
            .setConstraints(0, 0)
            .add(new HBox(fileField, selectFile))
            .setMargin(Insets(10, 10, 10, 10))
            .setConstraints(1, 0)
            .setHgrow(Priority.Always)
            .add(new HBox(10,
              progressBar,
              new TilePane {
                orientation = Orientation.Horizontal
                hgap = 10
                vgap = 8
                children.addAll(
                  new Button("Analyze!") {
                    onAction = (e: ActionEvent) => { mainController.startAnalysis(fileField.text.value, result); refresher.play() }
                    maxWidth = 100
                    disable.bind(Supervisor.isRunning.or(fileField.text.isEmpty()))
                  },

                  new Button("Abort!") {
                    onAction = (e: ActionEvent) => { mainController.stopAnalysis; refresher.pause() }
                    maxWidth = 100
                    disable.bind(Supervisor.isRunning.not())
                  })
              }))
            .setMargin(Insets(10, 10, 10, 10))
            .setConstraints(1, 1)
            .setHgrow(Priority.Always)
            .build

          // @formatter:on

        }
        VBox.setVgrow(content, Priority.Never)
        children.addAll(content, result)
      }

    }
    onCloseRequest = new EventHandler[javafx.stage.WindowEvent] {
      def handle(e: javafx.stage.WindowEvent) = { Supervisor.abort; Platform.exit() }
    }
  }

  val mainController = new MainController(stage)
}
