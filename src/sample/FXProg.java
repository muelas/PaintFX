package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FXProg extends Application {

    private Button redoB;
    private Button undoB;
    private Canvas canvas;
    private final EventHandler<ActionEvent> canvasToFront = event -> canvas.toFront();
    private final EventHandler<ActionEvent> canvasToBack = event -> canvas.toBack();

    public static void main(String[] args) {
        launch(args);
    }

    ArrayList<Shape> history;
    ArrayList<Shape> redo;

    Consumer<Double> setX;
    Consumer<Double> setY;
    BooleanSupplier valid;

    @Override
    public void start(Stage primaryStage) {
        // init
        history = new ArrayList<>();
        redo = new ArrayList<>();
        Group center = new Group();

        // shapes
        ToggleGroup shapeSelect = new ToggleGroup();

        ToggleButton drawB = new ToggleButton("Draw");
        drawB.setSelected(true);
        ToggleButton elliB = new ToggleButton("Ellipse");
        drawB.setOnAction(canvasToFront);
        ToggleButton rectB = new ToggleButton("Rectangle");
        rectB.setOnAction(canvasToFront);
        ToggleButton lineB = new ToggleButton("Line");
        lineB.setOnAction(canvasToFront);
        ToggleButton moveB = new ToggleButton("Move");
        moveB.setOnAction(canvasToBack);

        initShapeSelectToggleGroup(shapeSelect, drawB, elliB, rectB, lineB, moveB);
        FlowPane buttons = new FlowPane(Orientation.HORIZONTAL, 5, 5, drawB, elliB, rectB, lineB, new Separator(Orientation.VERTICAL), moveB);

        // color
        ColorPicker lineCol = new ColorPicker(Color.BLACK);
        Label lineColLabel = new Label("Line: ");
        ColorPicker fillCol = new ColorPicker(Color.BLACK);
        Label fillColLabel = new Label("Fill: ");
        CheckBox fillCB = new CheckBox("Fill");
        fillCB.setSelected(true);
        FlowPane color = new FlowPane(5, 5, lineColLabel, lineCol, fillColLabel, fillCol, new Separator(Orientation.VERTICAL), fillCB);

        // config
        redoB = new Button("Redo");
        redoB.setDisable(true);
        redoB.setOnAction(event -> {
            Shape s = redo.remove(redo.size() - 1);
            if (redo.isEmpty()) {
                redoB.setDisable(true);
            }
            history.add(s);
            center.getChildren().add(s);
            undoB.setDisable(false);
        });
        undoB = new Button("Undo");
        undoB.setDisable(true);
        undoB.setOnAction(event -> {
            Shape s = history.remove(history.size() - 1);
            if (history.isEmpty())
                undoB.setDisable(true);
            center.getChildren().remove(s);
            redo.add(s);
            redoB.setDisable(false);
        });
        FlowPane config = new FlowPane(5, 5, undoB, redoB);

        // top = shapes + color + config
        VBox top = new VBox(5, buttons, color, config);

        // Canvas
        canvas = new Canvas();
        canvas.setOpacity(0);   // invisible
        canvas.heightProperty().bind(primaryStage.heightProperty());    // bind size of canvas to windows size
        canvas.widthProperty().bind(primaryStage.widthProperty());
        canvas.setOnMousePressed(event -> {
//            System.out.println("Pressed: " + event.getX() + "/" + event.getY());

            if(moveB.isSelected()) {
                System.out.println("move --> canvas go back");
                canvas.toBack();
            } else if (lineB.isSelected()) {
                Line l = new Line(event.getX(), event.getY(), event.getX(), event.getY());
                l.setStroke(lineCol.getValue());
                setX = l::setEndX;
                setY = l::setEndY;
                valid = () -> true;
                center.getChildren().addAll(l);
                addShapeToHistory(l);
            } else if (elliB.isSelected()) {
                Ellipse e = new Ellipse(event.getX(), event.getY(), 0, 0);
                e.setStroke(lineCol.getValue());
                if (fillCB.isSelected())
                    e.setFill(fillCol.getValue());
                else
                    e.setFill(Color.TRANSPARENT);
                setX = x -> {
                    double newRadX = (x - e.getCenterX()) / 2;
                    e.getTransforms().addAll(new Translate(newRadX - e.getRadiusX(), 0, 0));
//                    e.setCenterX(e.getCenterX() + (newRadX - e.getRadiusX()));
                    e.setRadiusX(newRadX);
                };
                setY = y -> {
                    double newRadY = (y - e.getCenterY()) / 2;
                    e.getTransforms().addAll(new Translate(0, newRadY - e.getRadiusY(), 0));
//                    e.setCenterY(e.getCenterY() + (newRadY - e.getRadiusY()));
                    e.setRadiusY(newRadY);
                };
                valid = () -> {
                    return e.getRadiusX() > 0 && e.getRadiusY() > 0;
                };
                center.getChildren().addAll(e);
                addShapeToHistory(e);
            } else if (rectB.isSelected()) {
                Rectangle r = new Rectangle(event.getX(), event.getY(), 0, 0);
                r.setStroke(lineCol.getValue());
                if (fillCB.isSelected())
                    r.setFill(fillCol.getValue());
                else
                    r.setFill(Color.TRANSPARENT);
                setX = x -> {
                    r.setWidth(x - r.getX());
                };
                setY = y -> {
                    r.setHeight(y - r.getY());
                };
                valid = () -> {
                    return r.getHeight() > 0 && r.getWidth() > 0;
                };
                center.getChildren().addAll(r);
                addShapeToHistory(r);
            } else if (drawB.isSelected()) {
                Polyline p = new Polyline(event.getX(), event.getY());
                p.setStroke(lineCol.getValue());
                setX = x -> {
                    p.getPoints().addAll(x);
                };
                setY = y -> {
                    p.getPoints().addAll(y);
                };
                valid = () -> true;
                center.getChildren().addAll(p);
                addShapeToHistory(p);
            }
        });
        canvas.setOnMouseDragged(event -> {
//            System.out.println("Dragged: " + event.getX() + "/" + event.getY());
            if (setX != null) {
                setX.accept(event.getX());
                setY.accept(event.getY());
            }
        });
        canvas.setOnMouseReleased(event -> {
            setX = setY = null;

            Shape last = (history.isEmpty() ? null : history.get(history.size() - 1));
            if (last != null && valid != null && !valid.getAsBoolean()) {
                history.remove(history.size() - 1);
                center.getChildren().remove(last);
                System.out.println("Removing empty Shape: " + last);
            } else {
                undoB.setDisable(false);
            }

            canvas.toFront();
        });
        center.getChildren().addAll(canvas);

        BorderPane root = new BorderPane(center);
        root.setTop(top);

        String css = getClass().getResource("paint.css").toExternalForm();
        System.out.println(css);
        root.getStylesheets().add(css);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Paint");
        primaryStage.setHeight(600);
        primaryStage.setWidth(500);
        primaryStage.show();
    }

    double originX = -1, originY = -1;

    private void addShapeToHistory(Shape s) {
        history.add(s);
        redo.clear();
        redoB.setDisable(true);
        s.setOnMousePressed(event -> {
            originX = event.getSceneX();
            originY = event.getSceneY();
            System.out.println("pressed at " + event.getX() + "|" + event.getY());
        });
        s.setOnMouseDragged(event -> {
            double moveX = event.getSceneX() - originX;
            double moveY = event.getSceneY() - originY;
            System.out.println("drag at " + event.getSceneX() + "|" + event.getSceneY() + "with origin " + originX + "|" + originY + " --> move for " + moveX + "|" + moveY);
            s.getTransforms().addAll(new Translate(moveX, moveY));
            originX = event.getSceneX();
            originY = event.getSceneY();
        });
    }

    private void initShapeSelectToggleGroup(ToggleGroup shapeSelect, ToggleButton... buttons) {
        for (ToggleButton tb : buttons) {
            tb.setToggleGroup(shapeSelect);
        }
    }
}
