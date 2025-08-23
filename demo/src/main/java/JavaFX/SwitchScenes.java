package JavaFX;


import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;


public class SwitchScenes {
    public SwitchScenes(){}

    public void switchToGPT(MenuItem menuItem) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/AI.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) menuItem.getParentPopup().getOwnerWindow();//This allows FX to trace back to the window (stage).
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
    }
    public void switchToTasks(MenuItem menuItem) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/tasks.fxml"));
                Parent root = loader.load();
                TaskFX FXHandler = loader.getController();
                Stage stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                Platform.runLater(FXHandler::GETTasks);
            } catch (IOException | RuntimeException ex) {
                System.out.println("Something's up with the scene.");
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
            stage.show();
            Platform.runLater(FXHandler::GETNotebooks);
        } catch (IOException | RuntimeException ex) {
            System.out.println("Something's up with the scene.");
        }
    }
}
