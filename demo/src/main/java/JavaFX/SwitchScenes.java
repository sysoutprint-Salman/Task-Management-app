package JavaFX;


import SpringBoot.Task;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SwitchScenes {
    public enum Sort {A_Z, DUE_DATE, NEWEST, IMPORTANCE}

    public SwitchScenes(){}

    public void switchToGPT(MenuItem menuItem) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/AI.fxml"));
                Parent root = loader.load();
                AI_AssistantFX FXHandler = loader.getController();
                Stage stage = (Stage) menuItem.getParentPopup().getOwnerWindow();//This allows FX to trace back to the window (stage).
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                FXHandler.GETChatlogs();
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
    public <T> List<T> sort(List<T> list, Sort sortOption ){
        if (list.isEmpty()) return list;
        if (sortOption == null) return list;

        Object typeIdentity = list.get(0);
        if (typeIdentity instanceof Task){
            List<Task> taskList = (List<Task>) list;
            switch (sortOption){
                case A_Z:
                    taskList.sort(Comparator.comparing(Task::getTitle));
                    break;
                case DUE_DATE:
                    taskList.sort(Comparator.comparing(Task::getDate));
                    break;
                case NEWEST:
                    taskList.sort(Comparator.comparing(Task::getCreationDate).reversed());
                    break;
            }
            return (List<T>) taskList;
        }
        return Collections.emptyList();
    }

}
