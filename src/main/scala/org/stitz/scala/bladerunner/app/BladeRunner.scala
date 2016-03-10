package org.stitz.scala.bladerunner.app
import scala.collection.JavaConversions._
import scalafx.Includes.eventClosureWrapperWithParam
import scalafx.Includes.jfxActionEvent2sfx
import scalafx.Includes.observableList2ObservableBuffer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.value.ObservableValue
import scalafx.event.ActionEvent
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

object BladeRunner extends JFXApp {

  stage = new PrimaryStage {
    title = "Blade Runner - eliminate replicas"
    scene = new Scene(600, 600) {
      root = new VBox {
        val result = new TreeTableView[FileResultBean]() {
          columns ++= Seq(
            new TreeTableColumn[FileResultBean, String]("Path") {
              prefWidth = 400
              cellValueFactory = { p => ReadOnlyStringWrapper(p.value.value.value.path()) }
            },
            new TreeTableColumn[FileResultBean, Int]("Size") {
              prefWidth = 100
              cellValueFactory = { p => ReadOnlyIntegerWrapper(p.value.value.value.size()).asInstanceOf[ObservableValue[Int, Int]] }
              cellFactory = { _ =>
                new TreeTableCell(new javafx.scene.control.TreeTableCell[FileResultBean, Int] {

                  override def updateItem(item: Int, empty: Boolean) = {
                	  super.updateItem(item, empty)
                    if (!empty && item > 0 && getTreeTableRow() != null && getTreeTableRow().getTreeItem() != null) {
                      val bean = getTreeTableRow().getTreeItem().getValue()
                      val x = .5
                      val stops = Array(new Stop(0, Color.Green), new Stop(x, Color.White))
                      setBackground(new Background(new BackgroundFill(new LinearGradient(0,0,1,1,true, CycleMethod.NO_CYCLE, stops.toList), CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY)))
                      setText(item.toString())
                    }
                  }
                })
              }
            },
            new TreeTableColumn[FileResultBean, String]("Hash") {
              prefWidth = 100
              cellValueFactory = { p => ReadOnlyStringWrapper(p.value.value.value.hash()) }
            })
          tableMenuButtonVisible = true
        }
        javafx.scene.layout.VBox.setVgrow(result, Priority.Always)

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
            .add(new HBox(fileField, selectFile))
            .setMargin(Insets(10, 10, 10, 10))
            .setConstraints(1, 0)
            .setHgrow(Priority.Always)
            .add(new HBox(10,
              new Button("Start analysis!") {
                onAction = (e: ActionEvent) => mainController.startAnalysis(fileField.text.value, result)
              },
              new Button("Abort!") {
                onAction = (e: ActionEvent) => mainController.stopAnalysis
              }))
            .setMargin(Insets(10, 10, 10, 10))
            .setConstraints(1, 1)
            .build

          // @formatter:on
        }
        VBox.setVgrow(content, Priority.Never)

        children.addAll(content, result)
      }

    }
  }

  val mainController = new MainController(stage)
}
