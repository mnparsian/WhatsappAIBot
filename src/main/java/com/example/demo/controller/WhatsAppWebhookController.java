package com.example.demo.controller;

import com.example.demo.model.Document;
import com.example.demo.model.Organization;
import com.example.demo.repository.DocumentRepository;
import com.example.demo.service.AIService;
import com.example.demo.service.MessageLogService;
import com.example.demo.service.RAGService;
import com.example.demo.service.TenantService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppWebhookController {

  @Autowired private TenantService tenantService;

  @Autowired private AIService aiService;

  @Autowired private RAGService ragService;

  @Autowired private DocumentRepository documentRepository;
  @Autowired private MessageLogService messageLogService;

  @PostMapping("/webhook")
  public ResponseEntity<String> handleMessage(
      @RequestParam String message, @RequestParam String whatsappNumber) {
    try {

      // 1. Trovare l'organizzazione tramite il numero WhatsApp
      Organization organization = tenantService.getOrganizationByWhatsappNumber(whatsappNumber);
      System.out.println("organization:" + organization);

      // Registrare il messaggio in arrivo dall'utente
      messageLogService.logMessage(true, message, organization);


      // 2. Creazione del contesto tramite RAG (Documenti correlati)
      String context = ragService.buildContext(message, organization);
      System.out.println("context:" + context);

      // 3. Ottieni una risposta da OpenAI utilizzando il contesto
      String aiResponse = aiService.generateResponse(message, context);

      // Registra la risposta del modello all'utente
      messageLogService.logMessage(false, aiResponse, organization);


      // 4. Invia risposte a WhatsApp con Twilio o qualsiasi altro servizio
      return ResponseEntity.ok("Message sent: " + aiResponse);
    } catch (IllegalArgumentException e) {
      // Se l'organizzazione non viene trovata
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
    } catch (Exception e) {
      // Se si verifica un errore generale
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred.");
    }
  }

  @PostMapping("/upload")
  public ResponseEntity<String> uploadDocument(
      @RequestParam("file") MultipartFile file,
      @RequestParam("whatsappNumber") String whatsappNumber) {

    try {
      Organization org = tenantService.getOrganizationByWhatsappNumber(whatsappNumber);

      String content;
      String contentType = file.getContentType();

      if (contentType != null && contentType.equals("application/pdf")) {
        // Estrai testo da PDF con PDFBox
        try (PDDocument pdf = PDDocument.load(file.getInputStream())) {
          PDFTextStripper stripper = new PDFTextStripper();
          content = stripper.getText(pdf);
        }
      } else {
        // File di testo normale
        content =
            new Scanner(file.getInputStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next();
      }

      Document doc = new Document();
      doc.setOrganization(org);
      doc.setFilename(file.getOriginalFilename());
      doc.setContent(content);
      doc.setUploadedAt(LocalDateTime.now());

      Document saved = documentRepository.save(doc);

      ragService.upsertDocumentEmbeddings(org, saved);

      return ResponseEntity.ok("Document uploaded and indexed successfully.");

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Organization not found.");
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed.");
    }
  }
}
