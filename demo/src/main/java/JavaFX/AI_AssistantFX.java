package JavaFX;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AI_AssistantFX {
    public MenuItem gptMenuItem;
    public VBox chatBoxVbox;
    public Button sendButton;
    public TextField userTextField;
    private final DateTimeFormatter dateAndTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm a");
    protected final SwitchScenes handler = new SwitchScenes();
    private final LocalDateTime now = LocalDateTime.now();

    public MenuItem mainTasks;
    public MenuItem viewNotebook;

    public AI_AssistantFX(){}

    public String speakToGPT(@NotNull String userPrompt) {
        String timeNow = now.format(dateAndTimeFormatter);
        String gptKey = System.getenv("gptKey");
        String content = String.format(
                "{\"role\":\"assistant\",\"content\":\"%s\"}",
                userPrompt.concat(" Make your response into paragraphs IF needed with a maximum word count of 125. " +
                        "Follow the users request to the letter.")
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
            return innerJson.getString("content");

        } catch (InterruptedException | IOException ex) {
            System.err.println("Request was interrupted: " + ex.getMessage());
            Thread.currentThread().interrupt();  // Restore interrupted status
        }
        return "Error";
    }
    public void onSendMessage() {
        String prompt = userTextField.getText();
        if (!prompt.isEmpty()) {
            Label userPrompt = new Label(prompt);
            userTextField.clear();
            userPrompt.setWrapText(true);
            //userPrompt.maxWidthProperty().bind(chatBoxVbox.widthProperty());
            userPrompt.wrapTextProperty();
            userPrompt.getStyleClass().add("prompt");
            chatBoxVbox.getChildren().add(userPrompt);
        }
            String contentString = speakToGPT(prompt);
            Label gptResponseLabel = new Label(now.format(dateAndTimeFormatter) + "\n" + contentString + " - AI Assistant");

            gptResponseLabel.setWrapText(true);
            gptResponseLabel.wrapTextProperty();
            gptResponseLabel.setPadding(new Insets(10));
            //gptResponseLabel.maxWidthProperty().bind(chatBoxVbox.widthProperty());
            gptResponseLabel.getStyleClass().add("response");
            chatBoxVbox.getChildren().add(gptResponseLabel);
    }

    public void switchToTasks() {
        handler.switchToTasks(mainTasks);
    }

    public void switchToNotebook() {
        handler.switchToNotebook(viewNotebook);
    }
}
