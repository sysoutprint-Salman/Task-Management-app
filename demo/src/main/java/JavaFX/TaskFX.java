package JavaFX;

import SpringBoot.DeletedTask;
import SpringBoot.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskFX{
    @FXML
    public VBox mainTaskVbox;
    @FXML
    public VBox DTvbox;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    private final HTTPHandler httpHandler = new HTTPHandler();
    private Task.Status status;
    protected final SwitchScenes handler = new SwitchScenes();
    public Label taskLabel;
    public Button createTaskButton;
    private Timer timer = new Timer();
    private boolean isTaskScheduled = false;
    private final int DELAY = 1000;
    private final ObjectMapper mapper = new ObjectMapper();
    private enum Sort {A_Z, DUE_DATE, NEWEST}
    private Sort currentSortOption;
    private ToggleGroup sortGroup = new ToggleGroup();

    public MenuItem gptMenuItem;
    public MenuItem viewNotebook;
    public RadioMenuItem  A_Z;
    public RadioMenuItem  Due_Date;
    public RadioMenuItem  Newest;

    public TaskFX(){}
    public void createTask() {
        Stage createTaskStage = new Stage();
        createTaskStage.setTitle("Create New Task");

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        DatePicker picker = new DatePicker();
        picker.setPromptText("mm/dd/yyyy");

        /*RadioButton am = new RadioButton("AM");
        RadioButton pm = new RadioButton("PM");
        ToggleGroup toggleGroup = new ToggleGroup();
        am.setToggleGroup(toggleGroup);
        pm.setToggleGroup(toggleGroup);
        comboBoxTimes().setPrefWidth(80);
        comboBoxTimes().setEditable(true);*/
        picker.setPrefWidth(100);

        HBox timeHbox = new HBox(10, picker); //, comboBoxTimes(), am, pm);
        timeHbox.getStyleClass().add("pickers_hbox");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");

        Button createButton = new Button("Create");
        createButton.setOnAction(event -> {
            String title = titleField.getText();
            LocalDate date = picker.getValue();
            String description = descriptionArea.getText();
            if (title != null && picker.getValue() != null && descriptionArea.getText() != null) {
                String taskJson = String.format(
                        "{\"title\":\"%s\", \"date\":\"%s\", \"description\":\"%s\", \"status\":\"POSTED\"}",
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
        createTaskVbox.getStyleClass().add("create_task_vbox");

        Scene createTaskScene = new Scene(createTaskVbox, 350, 300);
        createTaskStage.setScene(createTaskScene);
        createTaskStage.show();
    }
    public void GETTasks() {
        try {
            status = Task.Status.POSTED;
            mainTaskVbox.getChildren().clear();

            //Temp solution: it's wiser to GET based on status defined in the controller and not here
            List<Task> tasks = httpHandler.GET("tasks/" + status, Task.class);
            tasks = sort(tasks,currentSortOption); //Default sort from
            tasks.forEach((task -> {
                TitledPane createdTaskPane = new TitledPane();
                RadioButton radio = new RadioButton(); radio.setPrefWidth(30);
                ToggleGroup group = new ToggleGroup();
                radio.setToggleGroup(group);
                Button dateButton  = new Button("Due: " + task.getDate().format(dateFormatter));
                Label taskTitle = new Label(task.getTitle()); taskTitle.setPrefWidth(290);
                TextArea descriptionArea = new TextArea(task.getDescription());
                HBox taskHbox = new HBox();

                taskHbox.getStyleClass().add("task_hbox");
                taskHbox.getChildren().addAll(radio, taskTitle, dateButton);
                taskHbox.setAlignment(Pos.CENTER);

                dateButton.setOnAction(e -> {
                });
                //createdTaskPane.setText(task.getTitle());
                createdTaskPane.setExpanded(false);
                createdTaskPane.setPrefWidth(500);
                createdTaskPane.setGraphic(taskHbox);
                createdTaskPane.getStyleClass().add("task");


                descriptionArea.setPrefHeight(50);
                descriptionArea.setWrapText(true);
                VBox taskContent = new VBox(new Label("Description:"), descriptionArea);
                taskContent.setSpacing(8);
                descriptionArea.maxWidthProperty().bind(taskContent.widthProperty());
                descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
                    descriptionArea.setPrefHeight(
                            descriptionArea.getFont().getSize() * (descriptionArea.getParagraphs().size() + 1) + 20
                    );
                });

                createdTaskPane.setContent(taskContent);
                createdTaskPane.setUserData(task.getId());
                mainTaskVbox.getChildren().add(createdTaskPane);
                taskLabel.setText("Tasks");
                radio.setOnAction(f -> {
                    if (radio.isSelected()){
                        status = Task.Status.COMPLETED;
                        String json = String.format("{\"status\":\"%s\"}", status);
                        httpHandler.UPDATE(json,"tasks/" + task.getId() + "/modular?section=status");
                        mainTaskVbox.getChildren().remove(createdTaskPane);
                    }
                });
                descriptionArea.setOnKeyReleased(e->{
                    autoUpdateDescription(descriptionArea,task.getId());
                });
                dateButton.setOnAction(e -> { editDate(task.getId());});
                A_Z.setToggleGroup(sortGroup);
                Due_Date.setToggleGroup(sortGroup);
                Newest.setToggleGroup(sortGroup);

                A_Z.setOnAction(e -> {
                    this.currentSortOption = Sort.A_Z;
                    GETTasks();
                });
                Newest.setOnAction(e ->{
                    this.currentSortOption = Sort.NEWEST;
                    GETTasks();
                });
                Due_Date.setOnAction(e ->{
                    this.currentSortOption = Sort.DUE_DATE;
                    GETTasks();
                });

                ContextMenu rightClickMenu = new ContextMenu();
                MenuItem completeItem = new MenuItem("Complete");
                MenuItem editItem = new MenuItem("Edit");
                MenuItem deleteItem = new MenuItem("Delete");
                rightClickMenu.getItems().addAll(completeItem, editItem, deleteItem);
                createdTaskPane.setOnContextMenuRequested(e -> {
                    rightClickMenu.show(createdTaskPane, e.getScreenX(), e.getScreenY());
                    completeItem.setOnAction(f -> {
                        status = Task.Status.COMPLETED;
                        String json = String.format("{\"status\":\"%s\"}", status);
                        httpHandler.UPDATE(json,"tasks/" + task.getId() + "/modular?section=status");
                        mainTaskVbox.getChildren().remove(createdTaskPane);
                    });
                    editItem.setOnAction(f -> {
                        editTask((Long) createdTaskPane.getUserData());
                    });
                    deleteItem.setOnAction(f -> {
                        status = Task.Status.DELETED;
                        String json = String.format("{\"status\":\"%s\"}", status);
                        httpHandler.UPDATE(json,"tasks/" + task.getId() + "/modular?section=status");
                        mainTaskVbox.getChildren().remove(createdTaskPane);
                    });
                });
            }));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void dummyTasks() {
        try {
            status = Task.Status.POSTED;
            mainTaskVbox.getChildren().clear();

            // Generate 5 dummy tasks for testing
            for (int i = 1; i <= 5; i++) {
                Task task = new Task();
                task.setId((long) i);
                task.setTitle("Dummy Task " + i);
                task.setDescription("This is the description for dummy task number " + i + ".");
                task.setDate(LocalDate.now().plusDays(i));
                task.setStatus(Task.Status.POSTED);

                TitledPane createdTaskPane = new TitledPane();
                RadioButton radio = new RadioButton();
                radio.setPrefWidth(30);
                ToggleGroup group = new ToggleGroup();
                radio.setToggleGroup(group);
                DatePicker datePicker = new DatePicker(task.getDate());
                Label taskTitle = new Label(task.getTitle());
                taskTitle.setPrefWidth(290);
                TextArea descriptionArea = new TextArea(task.getDescription());
                HBox taskHbox = new HBox();

                taskHbox.getStyleClass().add("task_hbox");
                taskHbox.getChildren().addAll(radio, taskTitle, datePicker);
                taskHbox.setAlignment(Pos.CENTER);

                createdTaskPane.setExpanded(false);
                createdTaskPane.setPrefWidth(500);
                createdTaskPane.setGraphic(taskHbox);
                createdTaskPane.getStyleClass().add("task");

                descriptionArea.setPrefHeight(50);
                descriptionArea.setWrapText(true);

                // disable manual typing
                datePicker.getEditor().setDisable(true);

                VBox taskContent = new VBox(new Label("Description:"), descriptionArea);
                taskContent.setSpacing(8);
                descriptionArea.maxWidthProperty().bind(taskContent.widthProperty());
                descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
                    descriptionArea.setPrefHeight(
                            descriptionArea.getFont().getSize() * (descriptionArea.getParagraphs().size() + 1) + 20
                    );
                });

                createdTaskPane.setContent(taskContent);
                createdTaskPane.setUserData(task.getId());
                mainTaskVbox.getChildren().add(createdTaskPane);
                taskLabel.setText("Tasks");

                // Right click menu (no http calls, just UI testing)
                ContextMenu rightClickMenu = new ContextMenu();
                MenuItem completeItem = new MenuItem("Complete");
                MenuItem editItem = new MenuItem("Edit");
                MenuItem deleteItem = new MenuItem("Delete");
                rightClickMenu.getItems().addAll(completeItem, editItem, deleteItem);
                createdTaskPane.setOnContextMenuRequested(e -> {
                    rightClickMenu.show(createdTaskPane, e.getScreenX(), e.getScreenY());
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void editTask(Long id) {
        Stage editStage = new Stage();
        editStage.setTitle("Edit Task");

        TextField editTitle = new TextField();
        editTitle.setPromptText("Task Title");

        DatePicker editPicker = new DatePicker();
        editPicker.setPromptText("mm/dd/yyyy");
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
    public void editDate(Long id){
        Stage editStage = new Stage();
        editStage.setTitle("Edit Date");

        DatePicker editPicker = new DatePicker();
        editPicker.setPromptText("mm/dd/yyyy");
        editPicker.setPrefWidth(100);
        editPicker.setEditable(false);

        TextArea editDescriptionArea = new TextArea();
        editDescriptionArea.setPromptText("Description");
        Button editButton = new Button("Edit");

        editButton.setOnAction(event -> {
            LocalDate date = editPicker.getValue();
            String updatedJson = String.format(
                    "{\"date\":\"%s\"}", date != null ? date.toString() : "");
            if (date != null) {httpHandler.UPDATE(updatedJson,"tasks/" + id + "/modular?section=date");}
            else {return;}
            mainTaskVbox.getChildren().clear();
            Platform.runLater(this::GETTasks);
            editStage.close();
        });

        VBox editTaskFormLayout = new VBox(10, editPicker, editButton);
        editTaskFormLayout.setPadding(new Insets(20));

        Scene formScene = new Scene(editTaskFormLayout, 200, 100);
        editStage.setScene(formScene);
        editStage.show();
    }
    public void GETDeletedTasks() {
        try {
            status = Task.Status.DELETED;
            mainTaskVbox.getChildren().clear();
            taskLabel.setText("Deleted Tasks");
            createTaskButton.setVisible(false);
            List<Task> deletedTasks = httpHandler.GET("tasks/" + status, Task.class);

            for (Task dTask : deletedTasks) {
                TitledPane deletedTaskPane = new TitledPane();
                Label taskTitle = new Label("Deleted: " +dTask.getTitle()); taskTitle.setPrefWidth(290);
                TextArea descriptionArea = new TextArea(dTask.getDescription());
                HBox taskHbox = new HBox();

                taskHbox.getStyleClass().add("task_hbox");
                taskHbox.getChildren().addAll(taskTitle);
                taskHbox.setAlignment(Pos.CENTER);

                VBox content = new VBox(new Label("Due: " + dTask.getDate().format(dateFormatter)),
                        descriptionArea);
                content.setAlignment(Pos.TOP_LEFT);
                descriptionArea.maxWidthProperty().bind(content.widthProperty());
                descriptionArea.setDisable(true);
                descriptionArea.setWrapText(true);
                descriptionArea.setPrefHeight(50);

                deletedTaskPane.setExpanded(false);
                deletedTaskPane.setPrefWidth(500);
                deletedTaskPane.setContent(content);
                deletedTaskPane.setUserData(dTask.getId());
                deletedTaskPane.getStyleClass().add("task");
                deletedTaskPane.setGraphic(taskHbox);
                mainTaskVbox.getChildren().add(deletedTaskPane);

                ContextMenu rightClickMenu = new ContextMenu();
                MenuItem recoverItem = new MenuItem("Recover");
                rightClickMenu.getItems().add(recoverItem);
                recoverItem.setOnAction(f -> {
                    status = Task.Status.POSTED;
                    String json = String.format("{\"status\":\"%s\"}", status);
                    recoverItem.setDisable(true);
                    httpHandler.UPDATE(json,"tasks/" + dTask.getId() + "/modular?section=status");
                    mainTaskVbox.getChildren().remove(deletedTaskPane);
                });
                deletedTaskPane.setContextMenu(rightClickMenu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void GETCompletedTasks(){}
    public void autoUpdateDescription(TextArea notepadArea, Long taskId){
            if (isTaskScheduled) {
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
                            updateMap.put("description", updatedText);
                            String updatedJson = mapper.writeValueAsString(updateMap);
                            httpHandler.UPDATE(updatedJson,"tasks/" + taskId + "/modular?section=description");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    isTaskScheduled = false;
                }
            };
            timer.schedule(task, DELAY);
            isTaskScheduled = true;
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

    public void switchToGPT() {
        handler.switchToGPT(gptMenuItem);
    }
    public void switchToNotebook() {
        handler.switchToGPT(viewNotebook);
    }

}
