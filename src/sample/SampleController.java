package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;

public class SampleController {
    public Label helloLabel;

    public void sayHelloWorld(ActionEvent actionEvent) {
        helloLabel.setText("Hello World!");
    }
}
