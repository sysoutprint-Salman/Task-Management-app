package JavaFX;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FXTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Button btn = new Button("Hello JavaFX");
        btn.setOnAction(event -> System.out.println("Button Clicked!"));

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("JavaFX Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
