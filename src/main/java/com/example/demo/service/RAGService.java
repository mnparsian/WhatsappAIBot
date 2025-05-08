package com.example.demo.service;

import com.example.demo.model.Document;
import com.example.demo.model.DocumentChunk;
import com.example.demo.repository.DocumentChunkRepository;
import com.example.demo.repository.DocumentRepository;

import com.example.demo.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.http.*;
import java.net.URI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class RAGService {

  @Value("${openai.api.key}")
  private String openaiApiKey;

  @Autowired private DocumentRepository documentRepository;
  @Autowired private DocumentChunkRepository documentChunkRepository;

  public List<Float> getEmbedding(String text) throws IOException, InterruptedException {
    // Costruire un JSON valido con JSONObject
    JSONObject requestJson = new JSONObject();
    requestJson.put("input", text);
    requestJson.put("model", "text-embedding-ada-002");  // Modello valido e pubblico

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/embeddings"))
            .header("Authorization", "Bearer " + openaiApiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("OpenAI Embedding API Response: " + response.body());

    JSONObject json = new JSONObject(response.body());

    if (!json.has("data")) {
      throw new RuntimeException("OpenAI embedding request failed: " + json.toString());
    }

    JSONArray embeddingArray = json.getJSONArray("data").getJSONObject(0).getJSONArray("embedding");

    List<Float> embedding = new ArrayList<>();
    for (int i = 0; i < embeddingArray.length(); i++) {
      embedding.add((float) embeddingArray.getDouble(i));
    }

    return embedding;
  }


  public List<Long> searchSimilarDocuments(List<Float> embedding, String collectionName)
      throws IOException, InterruptedException {
    System.out.println("embedding"+embedding);
    System.out.println("collectionName"+collectionName);
    JSONObject requestJson = new JSONObject();
    requestJson.put("vector", embedding);
    requestJson.put("top", 3);
    requestJson.put("with_payload", true);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "http://localhost:6333/collections/" + collectionName + "/points/search"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
            .build();

    System.out.println("request:"+request);

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    System.out.println("client:"+client);
    JSONObject json = new JSONObject(response.body());
    System.out.println("Response.body:"+response.body());
    JSONArray results = json.getJSONArray("result");
    System.out.println("result:"+results);

    List<Long> documentIds = new ArrayList<>();
    for (int i = 0; i < results.length(); i++) {
      JSONObject payload = results.getJSONObject(i).getJSONObject("payload");
      long docId = payload.getLong("documentId");
      documentIds.add(docId);
    }
    System.out.println("documentIds:"+documentIds);
    return documentIds;
  }

  public void upsertDocumentEmbeddings(Organization org, Document doc) throws IOException, InterruptedException {
    createQdrantCollectionIfNotExists(org.getEmbeddingIndexId());

    String[] words = doc.getContent().split("\\s+");
    int chunkSize = 100;
    List<String> chunks = new ArrayList<>();

    for (int i = 0; i < words.length; i += chunkSize) {
      int end = Math.min(i + chunkSize, words.length);
      String chunk = String.join(" ", List.of(words).subList(i, end));
      chunks.add(chunk);
    }

    List<JSONObject> vectorPoints = new ArrayList<>();

    for (String chunkText : chunks) {
      // 1. Per prima cosa salva il blocco per ottenere un ID
      DocumentChunk chunkEntity = new DocumentChunk();
      chunkEntity.setContent(chunkText);
      chunkEntity.setDocument(doc);
      chunkEntity = documentChunkRepository.save(chunkEntity); // ⬅️ L'ID viene generato qui

      // 2. Creazione embedding
      List<Float> embedding = getEmbedding(chunkText);

      // 3. Salvare embedding in DB
      chunkEntity.setEmbeddingJson(new JSONArray(embedding).toString());
      documentChunkRepository.save(chunkEntity);

      // 4. Preparazione per Qdrant
      JSONObject payload = new JSONObject();
      payload.put("documentId", doc.getId());
      payload.put("chunkId", chunkEntity.getId());

      JSONObject vectorData = new JSONObject();
      vectorData.put("id", chunkEntity.getId());
      vectorData.put("vector", embedding);
      vectorData.put("payload", payload);

      vectorPoints.add(vectorData);
    }

    JSONArray points = new JSONArray(vectorPoints);
    JSONObject requestJson = new JSONObject();
    requestJson.put("points", points);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:6333/collections/" + org.getEmbeddingIndexId() + "/points"))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("Qdrant upsert response: " + response.body());
  }


  public void createQdrantCollectionIfNotExists(String collectionName) throws IOException, InterruptedException {
    String url = "http://localhost:6333/collections/" + collectionName;

    JSONObject vectorsConfig = new JSONObject();
    vectorsConfig.put("size", 1536); // per modello ada-002
    vectorsConfig.put("distance", "Cosine");

    JSONObject requestBody = new JSONObject();
    requestBody.put("vectors", vectorsConfig);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("Qdrant create collection response: " + response.body());
  }



//  public String buildContext(String userMessage, Organization org)
//      throws IOException, InterruptedException {
//    System.out.println("userMessage:"+userMessage);
//    System.out.println("Org:"+org);
//    List<Float> embedding = getEmbedding(userMessage);
//    List<Long> documentIds = searchSimilarDocuments(embedding, org.getEmbeddingIndexId());
//    System.out.println("Embedding:"+embedding);
//    System.out.println("documentsIds:"+documentIds);
//
//    List<String> contents =
//        documentIds.stream()
//            .map(id -> documentRepository.findById(id))
//            .filter(Optional::isPresent)
//            .map(opt -> opt.get().getContent())
//            .toList();
//
//    String context = String.join("\n---\n", contents);
//    return context;
//  }



  public String buildContext(String userMessage, Organization org)
          throws IOException, InterruptedException {
    System.out.println("userMessage: " + userMessage);
    System.out.println("Org: " + org.getEmbeddingIndexId());

    List<Float> embedding = getEmbedding(userMessage);
    JSONObject requestJson = new JSONObject();
    requestJson.put("vector", embedding);
    requestJson.put("top", 5);
    requestJson.put("with_payload", true);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:6333/collections/" + org.getEmbeddingIndexId() + "/points/search"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("Qdrant search response: " + response.body());

    JSONObject json = new JSONObject(response.body());
    if (!json.has("result")) {
      return "No result from vector DB.";
    }

    JSONArray results = json.getJSONArray("result");
    List<Long> chunkIds = new ArrayList<>();

    for (int i = 0; i < results.length(); i++) {
      JSONObject payload = results.getJSONObject(i).optJSONObject("payload");
      if (payload != null && payload.has("chunkId")) {
        chunkIds.add(payload.getLong("chunkId"));
      }
    }

    if (chunkIds.isEmpty()) {
      return "No relevant chunk found.";
    }

    List<String> contents = chunkIds.stream()
            .map(documentChunkRepository::findById)
            .filter(Optional::isPresent)
            .map(opt -> opt.get().getContent())
            .toList();

    String context = String.join("\n---\n", contents);
    System.out.println("Final context: " + context);
    return context;
  }


}
