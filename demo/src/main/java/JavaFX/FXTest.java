package JavaFX;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXTest extends Application {
    @Override
    public void start(Stage primaryStage) {
        Button openFormButton = new Button("New Task");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.getChildren().add(openFormButton);

        openFormButton.setOnAction(e -> {
            Stage formStage = new Stage();
            formStage.setTitle("Create New Task");

            // Form inputs
            TextField titleField = new TextField();
            titleField.setPromptText("Task Title");

            TextField timeField = new TextField();
            timeField.setPromptText("Due Time (e.g. 2025-04-15 14:00)");

            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Description");

            Button createButton = new Button("Create");

            // Placeholder action
            createButton.setOnAction(event -> {
                String title = titleField.getText();
                String time = timeField.getText();
                String description = descriptionArea.getText();

                System.out.println("Task Created:");
                System.out.println("Title: " + title);
                System.out.println("Time: " + time);
                System.out.println("Description: " + description);

                formStage.close(); // Close form after creation
            });

            VBox formLayout = new VBox(10, titleField, timeField, descriptionArea, createButton);
            formLayout.setPadding(new Insets(20));

            Scene formScene = new Scene(formLayout, 300, 300);
            formStage.setScene(formScene);
            formStage.show();
        });

        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Task Manager");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
