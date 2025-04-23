package JavaFX;


import SpringBoot.DeletedTask;
import SpringBoot.Task;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    @FXML //This annotation specifies that this is a controller method.
    public void createTask(){
        Stage createTaskStage = new Stage();
        createTaskStage.setTitle("Create New Task");

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        DatePicker picker = new DatePicker();
        picker.setPromptText("Date");

        //ToggleGroup group = new ToggleGroup();
        RadioButton am = new RadioButton("AM");
        RadioButton pm = new RadioButton("PM");
        comboBoxTimes().setPrefWidth(80);
        comboBoxTimes().setEditable(true);
        picker.setPrefWidth(100);

        HBox spinnerHbox = new HBox(10, picker, comboBoxTimes(), am, pm);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");

        Button createButton = new Button("Create");

        createButton.setOnAction(event -> {
            String title = titleField.getText();
            LocalDate date = picker.getValue();
            String description = descriptionArea.getText();
            POST(title,date,description);
            String amPmPressed = "";
            if (am.isSelected()){amPmPressed = am.getText();}
            if (pm.isSelected()){amPmPressed = pm.getText();}
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
            Label descriptionTextLabel = new Label(description);
            Label dateLabel = new Label("Due: " + date.format(formatter) + " at " + comboBoxTimes().getValue() + " " + amPmPressed);
            descriptionTextLabel.setWrapText(true);

            if (title == null || picker.getValue() == null || descriptionArea.getText() == null){
                Label warning = new Label("Any field can't be left empty.");
                return; //TODO make a preventative measure to stop creation of a task with an empty field
            }

            VBox descriptionVbox = new VBox(5);
            descriptionVbox.getChildren().addAll(dateLabel, descriptionTextLabel);

            TitledPane titledPaneTask = new TitledPane();
            titledPaneTask.setText(title);
            titledPaneTask.setCollapsible(true);
            titledPaneTask.setContent(descriptionVbox);

            ContextMenu rightClickMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("Edit Task");
            MenuItem deleteItem = new MenuItem("Delete Task");
            rightClickMenu.getItems().addAll(editItem, deleteItem);
            titledPaneTask.setOnContextMenuRequested(e ->{
                rightClickMenu.show(titledPaneTask,e.getScreenX(), e.getScreenY());
                deleteItem.setOnAction(f ->{
                    mainTaskVbox.getChildren().remove(titledPaneTask);
                });
                //This will show the right click menu options. It will show at the position where the mouse clicked it on.
            });

            createTaskStage.close();
            mainTaskVbox.getChildren().add(titledPaneTask);
        });

        VBox createTaskVbox = new VBox(10, titleField, spinnerHbox, descriptionArea, createButton);
        createTaskVbox.setPadding(new Insets(20));

        Scene createTaskScene = new Scene(createTaskVbox, 300, 300);
        createTaskStage.setScene(createTaskScene);
        createTaskStage.show();
    }

    public void POST(String title, LocalDate date, String description){
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
                System.out.println("Task successfully sent to backend.");
            } else {
                System.out.println("Failed to send task. HTTP Code: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void DELETE(Long id, String address){
        String url = "http://localhost:8080/" + address + "/" + id;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            if (response.statusCode() == 200) {
                System.out.println("Deletion successfully.");
            } else {
                System.out.println("Failed to delete.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void GETDeletedTasks(){

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/deleted_tasks")).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            ObjectMapper mapper = new ObjectMapper(); //Jackson
            mapper.registerModule(new JavaTimeModule());
            JsonNode node = mapper.readTree(json);
            List<DeletedTask> deletedTasks = new ArrayList<>();

            if (node.isArray()){
                deletedTasks = mapper.readValue(json, new TypeReference<List<DeletedTask>>() {});
            } else if (node.isObject()) {
                DeletedTask deletedTask = mapper.treeToValue(node,DeletedTask.class);
                deletedTasks.add(deletedTask);
            }

            for (DeletedTask dTask: deletedTasks){
                TitledPane deletedTaskPane = new TitledPane();
                deletedTaskPane.setText("Deleted: " + dTask.getTitle());
                deletedTaskPane.setExpanded(false);
                deletedTaskPane.setPrefWidth(500);
                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
                VBox content = new VBox(new Label("Due: " + dTask.getDate()), new Label(dTask.getDescription()));
                deletedTaskPane.setContent(content);
                deletedTaskPane.setUserData(dTask.getId());
                DTvbox.getChildren().add(deletedTaskPane);
                ContextMenu rightClickMenu = new ContextMenu();
                MenuItem recoverItem = new MenuItem("Recover Task");
                rightClickMenu.getItems().add(recoverItem);
                deletedTaskPane.setOnContextMenuRequested(e -> {
                    rightClickMenu.show(deletedTaskPane, e.getScreenX(), e.getSceneY());
                    recoverItem.setOnAction(f -> {
                        DELETE(dTask.getId(),"deleted-tasks");
                        POST(dTask.getTitle(), dTask.getDate(), dTask.getDescription());
                        DTvbox.getChildren().remove(deletedTaskPane);
                    });
                });
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void GETTasks(){
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

            if (node.isArray()){
                tasks = mapper.readValue(json, new TypeReference<List<Task>>() {});
            } else if (node.isObject()) {
                Task task = mapper.treeToValue(node,Task.class);
                tasks.add(task);
            }

            for (Task task: tasks){
                TitledPane createdTaskPane = new TitledPane();
                //createdTaskPane.setPrefSize(200,200);
                createdTaskPane.setText(task.getTitle());
                createdTaskPane.setExpanded(false);
                createdTaskPane.setPrefWidth(500);
                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
                VBox content = new VBox(new Label("Due: " + task.getDate()), new Label(task.getDescription()));
                createdTaskPane.setContent(content);
                createdTaskPane.setUserData(task.getId());
                mainTaskVbox.getChildren().add(createdTaskPane);

                ContextMenu rightClickMenu = new ContextMenu();
                MenuItem editItem = new MenuItem("Edit Task");
                MenuItem deleteItem = new MenuItem("Delete Task");
                rightClickMenu.getItems().addAll(editItem, deleteItem);
                createdTaskPane.setOnContextMenuRequested(e ->{
                    rightClickMenu.show(createdTaskPane,e.getScreenX(), e.getScreenY());
                    editItem.setOnAction(f ->{
                        editTaskStage((Long) createdTaskPane.getUserData());
                    });
                    deleteItem.setOnAction(f ->{
                        mainTaskVbox.getChildren().remove(createdTaskPane);
                        DELETE(task.getId(), "tasks");
                    });
                });
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void UPDATE(Long id, String title, LocalDate date, String description){
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
        }catch (InterruptedException |IOException e){
            e.printStackTrace();
        }
    }
    public void editTaskStage(Long id){
        Stage editStage = new Stage();
        editStage.setTitle("Edit Task");

        TextField editField = new TextField();
        editField.setPromptText("Task Title");

        DatePicker editPicker = new DatePicker();
        editPicker.setPromptText("Date");

        RadioButton editAm = new RadioButton("AM");
        RadioButton editPm = new RadioButton("PM");
        comboBoxTimes().setPrefWidth(80);
        comboBoxTimes().setEditable(true);
        editPicker.setPrefWidth(100);

        HBox editHBox = new HBox(10, editPicker, comboBoxTimes(), editAm, editPm);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");

        Button editButton = new Button("Edit");

        editButton.setOnAction(event -> {
            UPDATE(id, editField.getText(), editPicker.getValue(), descriptionArea.getText());
            editStage.close();
        });

        VBox editTaskFormLayout = new VBox(10, editField, editHBox, descriptionArea, editButton);
        editTaskFormLayout.setPadding(new Insets(20));

        Scene formScene = new Scene(editTaskFormLayout, 300, 300);
        editStage.setScene(formScene);
        editStage.show();

    }
    public ComboBox<String> comboBoxTimes(){
        ComboBox<String> choiceBoxTimes = new ComboBox<>();
        for (int hour = 1; hour < 13; hour++){
            int minutes = 0;
            while (minutes < 60){
                String time = hour + ":" + minutes;
                if (Integer.toString(minutes).length() == 1){
                    time = hour + ":" + "0".concat(Integer.toString(minutes));
                    choiceBoxTimes.getItems().add(time);
                }else {
                    choiceBoxTimes.getItems().add(time);
                }
                minutes += 5;
            }
        }
        return choiceBoxTimes;
    }

    public void switchToGPT(){
            gptMenuItem.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/showGPT.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) ((MenuItem) e.getSource()).getParentPopup().getOwnerWindow();
                    //This allows FX to trace back to the window (stage).
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            });
    }
    @FXML
    public void switchToDT(){
        DTmenuItem.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/showDeletedTasks.fxml"));
                Parent root = loader.load();
                Controller controller = loader.getController();
                Stage stage = (Stage) ((MenuItem) e.getSource()).getParentPopup().getOwnerWindow();
                Scene scene = new Scene(root);
                controller.GETDeletedTasks();
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void speakToGPT(String userPrompt){
        sendButton.setOnAction(e -> {
            String gptKey = System.getenv("gptKey");
            String content = String.format(
                    "{\"role\":\"assistant\",\"content\":\"%s\"}",
                    userPrompt
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
                Label gptResponseLabel = new Label(contentString + " - GPT");
                gptResponseLabel.setWrapText(true);
                chatBoxVbox.getChildren().add(gptResponseLabel);

            } catch (InterruptedException | IOException ex) {
                System.err.println("Request was interrupted: " + ex.getMessage());
                Thread.currentThread().interrupt();  // Restore interrupted status
            }
        });
    }
    @FXML
    private void onSendMessage() {
        String prompt = userTextField.getText();
        speakToGPT(prompt);
    }
}
