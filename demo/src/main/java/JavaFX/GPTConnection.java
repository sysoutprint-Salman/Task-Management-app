package JavaFX;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GPTConnection {
    public static void main(String[] args) {
        GPTConnection();
    }
    public static void GPTConnection(){
        String gptKey = System.getenv("gptKey");
        var body = """
             {
             "model": "gpt-4o-mini",
              "messages": [
                {
                "role": "assistant", 
                "content": "What tasks should I make as a HR manager?"
                }
              ]
            }""";
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
            System.out.println(response.body());
        } catch (InterruptedException | IOException e) {
            System.err.println("Request was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();  // Restore interrupted status
        }
}




}


