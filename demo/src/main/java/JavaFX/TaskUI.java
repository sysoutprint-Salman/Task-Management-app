package JavaFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TaskUI extends Application {
    public static void main(String[] args) {
        //SpringApplication.run(Main.class, args);
        launch(args);

    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/tasks.fxml"));
            Parent root = loader.load(); //Holds all the UI components from loader.
            FX FX = loader.getController();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Task Management App");
            primaryStage.setScene(scene);
            primaryStage.show();
            //FX.GETTasks();
            FX.dummyTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


