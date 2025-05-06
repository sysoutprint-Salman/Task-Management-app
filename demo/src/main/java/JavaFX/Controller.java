package JavaFX;


import SpringBoot.DeletedTask;
import SpringBoot.Task;
import ch.qos.logback.core.joran.conditional.IfAction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Controller {
    @FXML
    public VBox DTvbox;
    @FXML
    public VBox mainTaskVbox;
    public ScrollPane scrollPane;
    //public TitledPane titledPane;
    public MenuItem gptMenuItem;
    public VBox chatBoxVbox;
    public BorderPane borderPane;
    public Button sendButton;
    public TextField userTextField;
    public MenuBar gptMenuBar;
    public ScrollPane DTscrollpane;
    public Label DTlabel;
    public MenuItem DTmenuItem;
    public MenuItem mainTasks;
    String prompt;
    DateTimeFormatter dateAndTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a MM/dd/yy");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");

    @FXML
    public void createTask() {
        Stage createTaskStage = new Stage();
        createTaskStage.setTitle("Create New Task");

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        DatePicker picker = new DatePicker();
        picker.setPromptText("mm/dd/yyyy");

        RadioButton am = new RadioButton("AM");
        RadioButton pm = new RadioButton("PM");
        ToggleGroup toggleGroup = new ToggleGroup();
        am.setToggleGroup(toggleGroup);
        pm.setToggleGroup(toggleGroup);
        comboBoxTimes().setPrefWidth(80);
        comboBoxTimes().setEditable(true);
        picker.setPrefWidth(100);

        HBox timeHbox = new HBox(10, picker); //, comboBoxTimes(), am, pm);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");

        Button createButton = new Button("Create");

        createButton.setOnAction(event -> {
            String title = titleField.getText();
            LocalDate date = picker.getValue();
            String description = descriptionArea.getText();
            if (title != null && picker.getValue() != null && descriptionArea.getText() != null) {
                POST(title, date, description);
            } else {return;}
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mainTaskVbox.getChildren().clear();
            Platform.runLater(this::GETTasks);
            createTaskStage.close();
        });

        VBox createTaskVbox = new VBox(10, titleField, timeHbox, descriptionArea, createButton);
        createTaskVbox.setPadding(new Insets(20));

        Scene createTaskScene = new Scene(createTaskVbox, 350, 300);
        createTaskStage.setScene(createTaskScene);
        createTaskStage.show();
    }

    public void POST(String title, LocalDate date, String description) {
        String json = String.format(
                "{\"title\":\"%s\", \"date\":\"%s\", \"description\":\"%s\"}",
                title,
                date != null ? date.toString() : "", //Ternary statement, local date type needs to use .toString
                description
        );

        try {
            URL url = new URL("http://localhost:8080/tasks");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) { //Converts JSON to bytes, very important.
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Task successfully posted.");
            } else {
                System.out.println("Failed to send task. HTTP Code: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void DELETE(Long id, String address, String archive) {
        String url = "http://localhost:8080/" + address + "/" + id + "?archive=".concat(archive);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Response body: " + response.body());
            } else {System.out.println("Failed to delete.");}
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void GETDeletedTasks() {
        try {
            DTvbox.getChildren().clear();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/deleted-tasks")).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            ObjectMapper mapper = new ObjectMapper(); //Jackson
            mapper.registerModule(new JavaTimeModule());
            JsonNode node = mapper.readTree(json);
            List<DeletedTask> deletedTasks = new ArrayList<>();

            if (node.isArray()) {
                deletedTasks = mapper.readValue(json, new TypeReference<List<DeletedTask>>() {});
            } else if (node.isObject()) {
                DeletedTask deletedTask = mapper.treeToValue(node, DeletedTask.class);
                deletedTasks.add(deletedTask);
            }
            for (DeletedTask dTask : deletedTasks) {
                TitledPane deletedTaskPane = new TitledPane();
                deletedTaskPane.setText("Deleted: " + dTask.getTitle());
                deletedTaskPane.setExpanded(false);
                deletedTaskPane.setPrefWidth(500);
                Label deletedDate = new Label("Deleted at: " + dTask.getDeletedDate().format(dateAndTimeFormatter));
                Label descriptionLabel = new Label(dTask.getDescription());
                VBox content = new VBox(new Label("Due: " + dTask.getDate().format(dateFormatter)),
                        deletedDate, descriptionLabel);
                content.setAlignment(Pos.TOP_LEFT);
                descriptionLabel.maxWidthProperty().bind(content.widthProperty());
                descriptionLabel.setWrapText(true);
                deletedTaskPane.setContent(content);
                deletedTaskPane.setUserData(dTask.getId());
                DTvbox.getChildren().add(deletedTaskPane);

                ContextMenu rightClickMenu = new ContextMenu();
                MenuItem recoverItem = new MenuItem("Recover");
                rightClickMenu.getItems().add(recoverItem);
                recoverItem.setOnAction(f -> {
                    recoverItem.setDisable(true);
                    DELETE(dTask.getId(), "deleted-tasks", "true");
                    //Deleting from deleted-tasks will auto post to Task table
                    DTvbox.getChildren().remove(deletedTaskPane);
                });
                deletedTaskPane.setContextMenu(rightClickMenu);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void GETTasks() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/tasks")).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            JsonNode node = mapper.readTree(json);
            List<Task> tasks = new ArrayList<>();

            if (node.isArray()) {
                tasks = mapper.readValue(json, new TypeReference<List<Task>>() {
                });
            } else if (node.isObject()) {
                Task task = mapper.treeToValue(node, Task.class);
                tasks.add(task);
            }

            for (Task task : tasks) {
                TitledPane createdTaskPane = new TitledPane();
                //createdTaskPane.setPrefSize(200,200);
                createdTaskPane.setText(task.getTitle());
                createdTaskPane.setExpanded(false);
                createdTaskPane.setPrefWidth(500);
                Label descriptionLabel =  new Label(task.getDescription());
                VBox content = new VBox(new Label("Due: " + task.getDate().format(dateFormatter)),
                        descriptionLabel);
                descriptionLabel.setWrapText(true);
                descriptionLabel.maxWidthProperty().bind(content.widthProperty());
                createdTaskPane.setContent(content);
                createdTaskPane.setUserData(task.getId());
                mainTaskVbox.getChildren().add(createdTaskPane);

                ContextMenu rightClickMenu = new ContextMenu();
                MenuItem completeItem = new MenuItem("Complete");
                MenuItem editItem = new MenuItem("Edit");
                MenuItem deleteItem = new MenuItem("Delete");
                rightClickMenu.getItems().addAll(completeItem, editItem, deleteItem);
                createdTaskPane.setOnContextMenuRequested(e -> {
                    rightClickMenu.show(createdTaskPane, e.getScreenX(), e.getScreenY());
                    completeItem.setOnAction(f -> {
                        DELETE((Long) createdTaskPane.getUserData(), "tasks", "false");
                        mainTaskVbox.getChildren().remove(createdTaskPane);
                    });
                    editItem.setOnAction(f -> {
                        editTaskStage((Long) createdTaskPane.getUserData());
                    });
                    deleteItem.setOnAction(f -> {
                        mainTaskVbox.getChildren().remove(createdTaskPane);
                        DELETE(task.getId(), "tasks", "true");
                    });
                });
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void UPDATE(Long id, String title, LocalDate date, String description) {
        try {
            String url = "http://localhost:8080/tasks/" + id;
            String json = String.format( //Formatting into json
                    "{\"title\":\"%s\", \"date\":\"%s\", \"description\":\"%s\"}",
                    title,
                    date != null ? date.toString() : "",
                    description
            );
            HttpRequest request = HttpRequest.newBuilder() //Building the HTTP request
                    .uri(URI.create(url)).header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json)).build(); //Attaches json as the body
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); //Coverts byte stream to String
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void editTaskStage(Long id) {
        Stage editStage = new Stage();
        editStage.setTitle("Edit Task");

        TextField editTitle = new TextField();
        editTitle.setPromptText("Task Title");

        DatePicker editPicker = new DatePicker();
        editPicker.setPromptText("mm/dd/yyyy");

        RadioButton editAm = new RadioButton("AM");
        RadioButton editPm = new RadioButton("PM");
        comboBoxTimes().setPrefWidth(80);
        comboBoxTimes().setEditable(true);
        editPicker.setPrefWidth(100);

        HBox editHBox = new HBox(10, editPicker);

        TextArea editDescriptionArea = new TextArea();
        editDescriptionArea.setPromptText("Description");
        Button editButton = new Button("Edit");


        editButton.setOnAction(event -> {
            if (editTitle.getText() != null && editPicker.getValue() != null && editDescriptionArea.getText() != null) {
                UPDATE(id, editTitle.getText(), editPicker.getValue(), editDescriptionArea.getText());
            } else {return;}
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mainTaskVbox.getChildren().clear();
            Platform.runLater(this::GETTasks);
            editStage.close();
        });

        VBox editTaskFormLayout = new VBox(10, editTitle, editHBox, editDescriptionArea, editButton);
        editTaskFormLayout.setPadding(new Insets(20));

        Scene formScene = new Scene(editTaskFormLayout, 350, 300);
        editStage.setScene(formScene);
        editStage.show();
    }

    public ComboBox<String> comboBoxTimes() {
        ComboBox<String> choiceBoxTimes = new ComboBox<>();
        for (int hour = 1; hour < 13; hour++) {
            int minutes = 0;
            while (minutes < 60) {
                String time = hour + ":" + minutes;
                if (Integer.toString(minutes).length() == 1) {
                    time = hour + ":" + "0".concat(Integer.toString(minutes));
                    choiceBoxTimes.getItems().add(time);
                } else {
                    choiceBoxTimes.getItems().add(time);
                }
                minutes += 5;
            }
        }
        return choiceBoxTimes;
    }

    public void switchToGPT() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/showGPT.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) gptMenuItem.getParentPopup().getOwnerWindow();
                //This allows FX to trace back to the window (stage).
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
    }
    @FXML
    public void switchToDT() {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/showDeletedTasks.fxml"));
                    Parent root = loader.load();
                    Controller controller = loader.getController();
                    Stage stage = (Stage) DTmenuItem.getParentPopup().getOwnerWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                    Platform.runLater(controller::GETDeletedTasks);
                } catch (IOException | RuntimeException ex) {
                    System.out.println("Something's up with the scene.");
                }
        }

    public void speakToGPT(String userPrompt, String time) {
        String gptKey = System.getenv("gptKey");
        String content = String.format(
                "{\"role\":\"assistant\",\"content\":\"%s\"}",
                userPrompt.concat(" Make your response into paragraphs IF needed with a maximum word count of 125.")
        );
        String body = String.format("""
                 {
                 "model": "gpt-4o-mini",
                  "messages": [%s]
                }""", content);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        //HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            //System.out.println(response.body());
            String json = response.body();
            JSONObject gptJson = new JSONObject(json);
            JSONObject innerJson = gptJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message");
            String contentString = innerJson.getString("content");
            Label gptResponseLabel = new Label(time + "\n" + contentString + " - AI Assistant");
            gptResponseLabel.setWrapText(true);
            gptResponseLabel.wrapTextProperty();
            gptResponseLabel.setPadding(new Insets(10));
            gptResponseLabel.maxWidthProperty().bind(chatBoxVbox.widthProperty());
            gptResponseLabel.setStyle("-fx-border-color: grey; -fx-border-width: 0.5; -fx-padding: 10;");
            chatBoxVbox.getChildren().add(gptResponseLabel);

        } catch (InterruptedException | IOException ex) {
            System.err.println("Request was interrupted: " + ex.getMessage());
            Thread.currentThread().interrupt();  // Restore interrupted status
        }
    }

    public void switchToTasks() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/main.fxml"));
                Parent root = loader.load();
                Controller controller = loader.getController();
                Stage stage = (Stage) mainTasks.getParentPopup().getOwnerWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                Platform.runLater(controller::GETTasks);
            } catch (IOException | RuntimeException ex) {
                System.out.println("Something's up with the scene.");

            }
    }

    @FXML
    private void onSendMessage() {
        sendButton.setOnAction(e -> {
            LocalDateTime now = LocalDateTime.now();
            String time = now.format(dateAndTimeFormatter);
            String prompt = userTextField.getText();
            if (!prompt.isEmpty()) {
                speakToGPT(prompt, time);
                userTextField.clear();
            }
        });
    }
}
