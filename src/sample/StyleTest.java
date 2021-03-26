package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class StyleTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        BorderPane root=new BorderPane();

        TextArea t=new TextArea();
        t.setPrefColumnCount(30);
        t.setPrefRowCount(3);
        root.setCenter(t);

        Button b=new Button("Test");
        b.getStyleClass().add("mybutton");
        b.setOnAction(event -> t.setText("I clicked the styled button!"));
        root.setBottom(b);

        Scene sc=new Scene(root);
        String css=getClass().getResource("paint.css").toExternalForm();
        System.out.println(css);
        sc.getStylesheets().add(css);

        primaryStage.setScene(sc);
        primaryStage.show();
    }
}
