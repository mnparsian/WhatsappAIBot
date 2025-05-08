package com.example.demo.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AIService {

  @Value("${openai.api.key}")
  private String openaiApiKey;

  public String generateResponse(String userMessage, String context) throws IOException, InterruptedException {
    String prompt = "Answer the following question using ONLY the provided context. If the answer is not in the context, just say 'I don't know'.\n\n" +
            "Context:\n" + context + "\n\n" +
            "Question: " + userMessage;


    JSONArray messagesArray = new JSONArray();
    messagesArray.put(new JSONObject().put("role", "system").put("content", "You are a helpful assistant."));
    messagesArray.put(new JSONObject().put("role", "user").put("content", prompt));


    JSONObject requestJson = new JSONObject();
    requestJson.put("model", "gpt-3.5-turbo");
    requestJson.put("messages", messagesArray);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/chat/completions"))
            .header("Authorization", "Bearer " + openaiApiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("OpenAI Chat Response: " + response.body());

    JSONObject json = new JSONObject(response.body());
    return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
  }

}
