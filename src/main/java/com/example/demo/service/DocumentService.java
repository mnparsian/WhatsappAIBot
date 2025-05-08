package com.example.demo.service;

import com.example.demo.model.Document;
import com.example.demo.model.DocumentChunk;
import com.example.demo.repository.DocumentChunkRepository;
import com.example.demo.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired private DocumentChunkRepository chunkRepository;

    public List<Document> getDocumentsByWhatsappNumber(String whatsappNumber) {
        return documentRepository.findByOrganization_WhatsappNumber(whatsappNumber);
    }

    public void deleteDocument(Long docId) {
        List<DocumentChunk> chunks = chunkRepository.findByDocument_Id(docId);
        chunkRepository.deleteAll(chunks);
        documentRepository.deleteById(docId);
    }
}
