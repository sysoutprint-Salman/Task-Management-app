package JavaFX;


import SpringBoot.Task;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;


public class SwitchScenes {

    public SwitchScenes(){}

    public void switchScene(ActionEvent event, String fxmlPath, Consumer<Object> afterLoad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath + ".fxml"));
            Parent root = loader.load();

            Stage stage = null;
            Object source = event.getSource();

            if (source instanceof Node node) {
                // Buttons, Labels, etc.
                stage = (Stage) node.getScene().getWindow();
            } else if (source instanceof MenuItem menuItem) {
                // Regular MenuItem: get the window from its parent popup
                if (menuItem.getParentPopup() != null) {
                    stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
                }
                // If it's inside a MenuBar (no popup), fallback to target
                else if (event.getTarget() instanceof Node node) {
                    stage = (Stage) node.getScene().getWindow();
                }
            }

            if (stage == null) {
                throw new IllegalStateException("Could not resolve Stage from event source: " + source);
            }

            stage.setScene(new Scene(root));
            stage.show();

            afterLoad.accept(loader.getController());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void switchToLogin(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Task Management App");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException | RuntimeException ex) {
            System.err.println("Error trying to load switchToLogin.");
            ex.printStackTrace();
        }
    }
    public void switchToTasks(Stage curStage) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/tasks.fxml"));
                Parent root = loader.load();
                TaskFX FXHandler = loader.getController();
                Scene scene = new Scene(root);
                curStage.setTitle("Task Management App");
                curStage.setScene(scene);
                curStage.centerOnScreen();
                curStage.show();
                Platform.runLater(FXHandler::getByPosted);
            } catch (IOException | RuntimeException ex) {
                System.err.println("Error trying to load switchToTasks.");
                ex.printStackTrace();
            }
    }
    public void switchToNotebook(MenuItem menuItem){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/notebook.fxml"));
            Parent root = loader.load();
            NotebookFX FXHandler = loader.getController();
            Stage stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            Platform.runLater(FXHandler::GETNotebooks);
        } catch (IOException | RuntimeException ex) {
            System.out.println("Something's up with the scene.");
        }
    }

}
