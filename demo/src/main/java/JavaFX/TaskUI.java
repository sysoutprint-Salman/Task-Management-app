package JavaFX;

import SpringBoot.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.springframework.boot.SpringApplication;

import java.io.IOException;

public class TaskUI extends Application {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        launch(args);

    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/main.fxml"));
            Parent root = loader.load(); //Holds all the UI components from loader.
            Controller controller = loader.getController();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Task Management App");
            primaryStage.setScene(scene);
            primaryStage.show();
            controller.GETTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


