package JavaFX;

import SpringBoot.Notebook;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;

public class NotebookFX{
    public VBox tabsVbox;
    public ScrollPane tabsScrollPane, notebookScrollPane;
    public TextArea notepadArea = new TextArea();
    public MenuBar tabsMenuBar;
    private Timer timer = new Timer();
    private boolean isTaskScheduled = false;
    private final int DELAY = 2000;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HTTPHandler httpHandler = new HTTPHandler();
    protected final SwitchScenes handler = new SwitchScenes();

    public MenuItem gptMenuItem;
    public MenuItem mainTasks;

    public NotebookFX(){}

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
            if (isTaskScheduled) { //Debouncing: Clears any previous timers to avoid multiple timers firing
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
            timer.schedule(task, DELAY); //Schedules the next auto-save
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


    public void switchToTasks() {
        handler.switchToTasks(mainTasks);
    }
    public void switchToGPT() {
        handler.switchToGPT(gptMenuItem);
    }
}
