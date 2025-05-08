package com.example.demo.repository;

import com.example.demo.model.DocumentChunk;
import com.example.demo.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocument(Document document);
    List<DocumentChunk> findByDocument_Organization_Id(Long organizationId);
    List<DocumentChunk> findByDocument_Id(Long documentId);
}
