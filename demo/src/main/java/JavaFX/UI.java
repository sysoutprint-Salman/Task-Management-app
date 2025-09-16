package JavaFX;

import SpringBoot.Rest;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;


//Putting SpringApp & Rest annotations here didn't work likely because of Application extension.
public class UI extends Application {
    public static void main(String[] args) {
        SpringApplication.run(Rest.class, args);
        launch(args);

    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/tasks.fxml"));
            //FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/notebook.fxml"));
            Parent root = loader.load(); //Holds all the UI components from loader.
            TaskFX FXHandler = loader.getController();
            //NotebookFX FXHandler = loader.getController();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Task Management App");
            primaryStage.setScene(scene);
            primaryStage.show();
            FXHandler.GETTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


