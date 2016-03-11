package org.stitz.scala.bladerunner.app
import scala.collection.JavaConversions._
import scalafx.Includes.eventClosureWrapperWithParam
import scalafx.Includes.jfxActionEvent2sfx
import scalafx.Includes.observableList2ObservableBuffer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.value.ObservableValue
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.control.Button.sfxButton2jfx
import scalafx.scene.control.Label
import scalafx.scene.control.Label.sfxLabel2jfx
import scalafx.scene.control.TextField
import scalafx.scene.control.Tooltip.stringToTooltip
import scalafx.scene.control.TreeItem
import scalafx.scene.control.TreeTableColumn
import scalafx.scene.control.TreeTableView
import scalafx.scene.layout.ColumnConstraints.sfxColumnConstraints2jfx
import scalafx.scene.layout.GridPane
import scalafx.scene.layout.HBox
import scalafx.scene.layout.HBox.sfxHBox2jfx
import scalafx.scene.layout.VBox
import scalafx.geometry.Insets
import scalafx.geometry.HPos
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.scene.layout.Priority
import scalafx.beans.property.ReadOnlyIntegerWrapper
import scalafx.scene.control.TreeTableColumn.sfxTreeTableColumn2jfx
import scalafx.scene.control.TableCell
import scalafx.scene.control.TreeTableCell
import scalafx.scene.control.TreeTableCell.sfxTreeTableCell2jfx
import scalafx.scene.shape.Circle
import scalafx.scene.paint.Color
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.Stop
import scalafx.beans.property.ReadOnlyLongWrapper
import scalafx.application.Platform
import scalafx.scene.control.ProgressBar
import scalafx.scene.layout.Region
import scalafx.scene.layout.TilePane
import scalafx.geometry.Orientation
import scalafx.scene.control.Control
import javafx.scene.control.Control
import scalafx.scene.control.Control
import org.stitz.scala.bladerunner.file.Supervisor
import scalafx.event.EventHandler
import javafx.event.EventHandler
import scalafx.animation.Timeline
import scalafx.animation.KeyFrame
import scalafx.util.Duration
import scalafx.event.ActionEvent

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
                          setBackground(new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops.toList), CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY)))
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
                  new Button("Start analysis!") {
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
