package JavaFX;

import SpringBoot.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;


@Data
public class LogInFX {
    private final SwitchScenes switchScenes = new SwitchScenes();
    private final HTTPHandler httpHandler = new HTTPHandler();
    private final UserPrefs userPrefs = new UserPrefs();
    private TaskFX taskFX = new TaskFX();
    //Login
    public TextField userLogInField;
    public Button logInBut;
    public Hyperlink registerHL;
    public VBox loginVbox;

    //Register
    public TextField createUsername;
    public TextField enterEmail;
    public Button createAccBut;
    public Label feedbackLabel;
    public Hyperlink loginHL;

    public LogInFX(){}

    public void autoLogIn(Stage curStage){
        String storedUsername = userPrefs.getStoredUsername();
        String storedEmail = userPrefs.getStoredEmail();
        if (storedUsername != null || storedEmail != null){
            //Switch to tasks scene
            switchScenes.switchToTasks(curStage);
        } else {
            //Switch to Log scene
            switchScenes.switchToLogin();
        }
    }
    public void onLogIn(ActionEvent event) {
        String emailOrUsernameCredential = userLogInField.getText().trim();

        boolean existingUser = httpHandler.GET(
                "users/existing?username=" + emailOrUsernameCredential +
                        "&email=" + emailOrUsernameCredential
        );
        if (existingUser){
            userPrefs.saveToPref(emailOrUsernameCredential); //Saves username in registry for quick login
            switchScenes.switchScene(event, "tasks",controller ->{
                taskFX = (TaskFX) controller;
                taskFX.setUser(userPrefs.getSavedUser());
                taskFX.getByPosted();}
            );
        } else {
            Label notFoundMessage = new Label("Username or email not found, try again.");
            notFoundMessage.getStyleClass().add("notFoundMessage");
            loginVbox.getChildren().add(notFoundMessage);
        }
    }
    @FXML
    public void onRegister(ActionEvent event){
        String username = createUsername.getText();
        String email = enterEmail.getText();

        userPrefs.setUsername(username);
        userPrefs.setEmail(email.toLowerCase());

        userPrefs.registerUser();
        createUsername.clear(); enterEmail.clear();
        switchScenes.switchScene(event, "tasks",controller ->{
            taskFX = (TaskFX) controller;
            taskFX.setUser(userPrefs.getSavedUser());
            taskFX.getByPosted();}
        );
    }

    public void switchToLogin(ActionEvent event){ //Used to switch from register to login & back
        switchScenes.switchScene(event, "login", controller ->{});
    }
    public void switchToRegister(ActionEvent event){
        switchScenes.switchScene(event, "register", controller ->{});
    }


}
