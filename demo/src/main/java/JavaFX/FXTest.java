package JavaFX;

import SpringBoot.DeletedTask;
import SpringBoot.Main;
import SpringBoot.Task;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FXTest extends Application {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        launch(args);


    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/sample.fxml"));
            Parent root = loader.load();

            FXTest controller = loader.getController();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Task Management App");
            primaryStage.setScene(scene);
            primaryStage.show();
            controller.GETTasks();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    VBox deletedTaskVBox = new VBox(10);
    @FXML
    public VBox root;
    public TitledPane titledPane;

    @FXML //This annotation specifies that this is a controller method.
    public void createTask(){
        Stage formStage = new Stage();
        formStage.setTitle("Create New Task");

        // Form inputs
        javafx.scene.control.TextField titleField = new javafx.scene.control.TextField();
        titleField.setPromptText("Task Title");

        DatePicker picker = new DatePicker();
        picker.setPromptText("Date");
        ComboBox<String> choiceBoxTimes = new ComboBox<>();

        //One way of adding different times to the above choicebox.
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
        Spinner<String> amPmSpinner = new Spinner<>();
        amPmSpinner.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(
                FXCollections.observableArrayList("AM", "PM")));
        choiceBoxTimes.setPrefWidth(80);
        choiceBoxTimes.setEditable(true);
        amPmSpinner.setPrefWidth(100);
        picker.setPrefWidth(100);

        HBox spinnerHbox = new HBox(10, picker, choiceBoxTimes, amPmSpinner);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");

        Button createButton = new Button("Create");

        createButton.setOnAction(event -> {
            String title = titleField.getText();
            LocalDate date = picker.getValue();
            String description = descriptionArea.getText();
            POST(title,date,description);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
            Label descriptionText = new Label(description);
            Label dateLabel = new Label("Due: " + date.format(formatter) + " at " + choiceBoxTimes.getValue());
            descriptionText.setWrapText(true);

            if (title == null || picker.getValue() == null || descriptionArea.getText() == null){
                Label warning = new Label("Any field can't be left empty.");
                return; //TODO make a preventative measure to stop creation of a task with an empty field
            }

            VBox descriptionVbox = new VBox(5);
            descriptionVbox.getChildren().addAll(dateLabel, descriptionText);

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
                    root.getChildren().remove(titledPaneTask);
                });
                //This will show the right click menu options. It will show at the position where the mouse clicked it on.
            });


            formStage.close();
            root.getChildren().add(titledPaneTask);
                });

        VBox TaskFormLayout = new VBox(10, titleField, spinnerHbox, descriptionArea, createButton);
        TaskFormLayout.setPadding(new Insets(20));

        Scene formScene = new Scene(TaskFormLayout, 300, 300);
        formStage.setScene(formScene);
        formStage.show();
    }

    public void POST(String title, LocalDate date, String description){
        String json = String.format(
                "{\"title\":\"%s\", \"date\":\"%s\", \"description\":\"%s\"}",
                title,
                date != null ? date.toString() : "", //Tiernary statement, local date type needs to use .toString
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
    public void DELETE(Long taskId){
        String url = "http://localhost:8080/tasks/" + taskId;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            if (response.statusCode() == 200) {
                System.out.println("Task deleted successfully.");
            } else {
                System.out.println("Failed to delete task.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void GETDeletedTasks(){

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/tasks")).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            ObjectMapper mapper = new ObjectMapper(); //Jackson
            List<DeletedTask> deletedTasks = mapper.readValue(json, new TypeReference<List<DeletedTask>>() {});
            //Mapping json from the GET request to an arraylist

            //This will get from the DB and display each task from the deletedTask DB and will create a new titledpane for each.
            for (DeletedTask task: deletedTasks){
                TitledPane deletedTaskPane = new TitledPane();
                deletedTaskPane.setText(task.getTitle());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
                VBox content = new VBox(new Label("Due: " + formatter.format(task.getDate())), new Label(task.getDescription()));
                deletedTaskPane.setContent(content);
                deletedTaskPane.setUserData(task.getId());
                deletedTaskVBox.getChildren().add(deletedTaskPane);
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
            List<Task> Tasks = mapper.readValue(json, new TypeReference<List<Task>>() {});



            for (Task task: Tasks){
                TitledPane createdTaskPane = new TitledPane();
                createdTaskPane.setText(task.getTitle());
                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
                VBox content = new VBox(new Label("Due: " + task.getDate()), new Label(task.getDescription()));
                createdTaskPane.setContent(content);
                createdTaskPane.setUserData(task.getId());
                root.getChildren().add(createdTaskPane);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}


