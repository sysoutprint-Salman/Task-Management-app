package JavaFX;


import SpringBoot.DeletedTask;
import SpringBoot.Notebook;
import SpringBoot.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;


public class FX {
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
    public MenuItem viewNotebook;
    public String prompt;
    public MenuItem newTabsItem;
    public ScrollPane tabsScrollPane, notebookScrollPane;
    public VBox tabsVbox;
    public TextArea notepadArea = new TextArea();
    private final DateTimeFormatter dateAndTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a MM/dd/yy");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    public Timer timer = new Timer();
    boolean isTaskScheduled = false;
    public final int DELAY = 2000;
    public MenuBar tabsMenuBar;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;
    private final HTTPHandler httpHandler = new HTTPHandler();


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
                String taskJson = String.format(
                        "{\"title\":\"%s\", \"date\":\"%s\", \"description\":\"%s\"}",
                        title, date != null ? date.toString() : "", description
                );
                httpHandler.POST("tasks", taskJson);
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
    public void dummyTasks(){
        for (int i = 1; i <= 3; i++) {
            RadioButton radio = new RadioButton();
            TitledPane createdTaskPane = new TitledPane();
            createdTaskPane.setText("Dummy task " + i);
            createdTaskPane.setExpanded(false);
            createdTaskPane.setPrefWidth(500);
            //createdTaskPane.setStyle("-fx-collapsible: false;");
            createdTaskPane.setGraphic(radio);
            Button dateButton  = new Button("Due: Whenever" );
            TextArea descriptionArea =  new TextArea("This is a test description");
            descriptionArea.setPrefSize(20,20);
            descriptionArea.setWrapText(true);
            VBox content = new VBox(dateButton, descriptionArea);
            descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
                descriptionArea.setPrefHeight(
                        descriptionArea.getFont().getSize() * (descriptionArea.getParagraphs().size() + 1) + 20
                );
            });
            createdTaskPane.setContent(content);
            mainTaskVbox.getChildren().add(createdTaskPane);
        }
    }
    public void GETDeletedTasks() {
        try {
            DTvbox.getChildren().clear();
            List<DeletedTask> deletedTasks = httpHandler.GET("deleted-tasks", DeletedTask.class);

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
                    httpHandler.DELETE(dTask.getId(), "deleted-tasks", "true");
                    DTvbox.getChildren().remove(deletedTaskPane);
                });
                deletedTaskPane.setContextMenu(rightClickMenu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void GETTasks() {
        try {
            List<Task> tasks = httpHandler.GET("tasks", Task.class);
            tasks.forEach((task -> {
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
                        httpHandler.DELETE((Long) createdTaskPane.getUserData(), "tasks", "false");
                        mainTaskVbox.getChildren().remove(createdTaskPane);
                        //DELETE for tasks also posts automatically to deleted_tasks
                    });
                    editItem.setOnAction(f -> {
                        editTaskStage((Long) createdTaskPane.getUserData());
                    });
                    deleteItem.setOnAction(f -> {
                        mainTaskVbox.getChildren().remove(createdTaskPane);
                        httpHandler.DELETE(task.getId(), "tasks", "true");
                    });
                });
            }));

        } catch (Exception e) {
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
            String title = editTitle.getText();
            LocalDate date = editPicker.getValue();
            String description = editDescriptionArea.getText();
            String json = String.format(
                    "{\"title\":\"%s\", \"date\":\"%s\", \"description\":\"%s\"}",
                    title,
                    date != null ? date.toString() : "",
                    description
            );
            if (title != null && date != null && description != null) {
                httpHandler.UPDATE(json, "tasks/"+id);
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/AI.fxml"));
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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/deletedTasks.fxml"));
                    Parent root = loader.load();
                    FX FX = loader.getController();
                    Stage stage = (Stage) DTmenuItem.getParentPopup().getOwnerWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                    Platform.runLater(FX::GETDeletedTasks);
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/tasks.fxml"));
                Parent root = loader.load();
                FX FX = loader.getController();
                Stage stage = (Stage) mainTasks.getParentPopup().getOwnerWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                Platform.runLater(FX::GETTasks);
            } catch (IOException | RuntimeException ex) {
                System.out.println("Something's up with the scene.");
            }
    }
    public void switchToNotebook(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaFX/notebook.fxml"));
            Parent root = loader.load();
            FX FX = loader.getController();
            Stage stage = (Stage) viewNotebook.getParentPopup().getOwnerWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            Platform.runLater(FX::GETNotebooks);
        } catch (IOException | RuntimeException ex) {
            System.out.println("Something's up with the scene.");
        }
    }
    public void createNewTab(){
        Stage newTabStage = new Stage();
        newTabStage.setTitle("Create Tab");
        TextField newTabTitle = new TextField();
        Button createTabButton = new Button("Create Tab");
        createTabButton.setOnAction(e ->{
            String title = newTabTitle.getText();
            String notebookJson = String.format(
                    "{\"tabTitle\":\"%s\"}", title);
            httpHandler.POST("notebooks", notebookJson);
            newTabStage.close();
        });

        VBox newTabVbox = new VBox(10, newTabTitle, createTabButton);
        newTabVbox.setPadding(new Insets(20));

        Scene newTabScene = new Scene(newTabVbox, 320, 150);
        newTabStage.setScene(newTabScene);
        newTabStage.show();
    }
    public void editNewTab(String oldText, Long id){
        Stage editTabStage = new Stage();
        editTabStage.setTitle("Edit Tab");
        TextField editTabTitle = new TextField(); editTabTitle.setPromptText(oldText);
        Button editTabButton = new Button("Edit Tab");
        editTabButton.setOnAction(e ->{
            String title = editTabTitle.getText();
            String notebookJson = String.format(
                    "{\"tabTitle\":\"%s\"}", title);
            httpHandler.UPDATE(notebookJson, "notebooks/" + id + "/tab");
            tabsVbox.getChildren().clear();
            Platform.runLater(this::GETNotebooks);
            editTabStage.close();
        });
        VBox editTabVbox = new VBox(10, editTabTitle, editTabButton);
        editTabVbox.setPadding(new Insets(20));
        Scene editTabScene = new Scene(editTabVbox, 320, 150);
        editTabStage.setScene(editTabScene);
        editTabStage.show();

    }
    public void GETNotebooks(){
        try {
            List<Notebook> notebooks = httpHandler.GET("notebooks",Notebook.class);
            notebookScrollPane.setContent(notepadArea);
            notepadArea.setVisible(false);
            notepadArea.setWrapText(true);
            Menu tabsMenuCompo = new Menu("Tabs");
            tabsMenuBar = new MenuBar(); tabsMenuBar.getMenus().add(tabsMenuCompo);
            tabsVbox.getChildren().add(tabsMenuBar);
            notebooks.forEach((notebook ->{
                TitledPane createdTab = new TitledPane();
                createdTab.setText(notebook.getTabTitle());
                createdTab.setCollapsible(false);
                createdTab.setOnMouseClicked(e ->{
                    notepadArea.setText(notebook.getNotebookText());
                    notepadArea.setVisible(true);
                    autoUpdateNotebookText(notebook.getId());
                });
                ContextMenu contextMenu = new ContextMenu();
                MenuItem editTab = new MenuItem("Edit Tab");
                MenuItem deleteTab = new MenuItem("Delete Tab");
                contextMenu.getItems().addAll(editTab, deleteTab);
                editTab.setOnAction(event -> {
                    editNewTab(notebook.getTabTitle(), notebook.getId());
                });
                deleteTab.setOnAction(event -> {
                    httpHandler.DELETE(notebook.getId(), "notebooks", "false");
                    tabsVbox.getChildren().remove(createdTab);
                    notepadArea.setVisible(false);
                });
                createdTab.setContextMenu(contextMenu);
                tabsVbox.getChildren().add(createdTab);
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    } //TODO add delete and edit functions to tabs.
    public void autoUpdateNotebookText(Long notebookId){
        notepadArea.setOnKeyReleased(e ->{
            if (isTaskScheduled) { //Debouncing
                timer.cancel();
                timer = new Timer();
            }
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        try {
                            String updatedText = notepadArea.getText();
                            Map<String, String> updateMap = new HashMap<>();
                            updateMap.put("notebookText", updatedText);
                            String updatedJson = mapper.writeValueAsString(updateMap);
                            //Converts map to json and handles newlines and special characters
                            httpHandler.UPDATE(updatedJson, "notebooks/" + notebookId + "/text");
                            tabsVbox.getChildren().clear();
                            GETNotebooks();
                            notepadArea.setVisible(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });
                    isTaskScheduled = false;
                }
            };
            timer.schedule(task, DELAY);
            isTaskScheduled = true;
        });
    }
    public TitledPane coloredTabs(TitledPane tab, Label newTabTitle){
        ColorPicker colorPicker = new ColorPicker();

        Color color = colorPicker.getValue();
        String hex = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
        String textColor = luminance < 0.5 ? "white" : "black";

        Label styledText = new Label(newTabTitle.getText());
        styledText.setStyle("-fx-text-fill: " + textColor + ";");

        tab.setGraphic(styledText);
        tab.setCollapsible(false);
        tab.setStyle("-fx-background-color: " + hex + ";");


        Platform.runLater(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            Node titleNode = tab.lookup(".title");
            if (titleNode != null) {
                titleNode.setStyle("-fx-background-color: " + hex + ";");
            } else {
                System.out.println("Title node not found.");
            }
        });
        return new TitledPane();
    } //TODO, work on having tabs be colored
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
