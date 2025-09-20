package JavaFX;

import SpringBoot.Rest;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.prefs.Preferences;


//Putting SpringApp & Rest annotations here didn't work likely because of Application extension.
@Slf4j
public class UI extends Application {
    private final LogInFX logInFX = new LogInFX();
    public static void main(String[] args) {
        SpringApplication.run(Rest.class, args);
        launch(args);

    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            primaryStage.setTitle("Task Management App");
            logInFX.autoLogIn(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


