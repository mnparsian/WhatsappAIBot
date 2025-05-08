package com.example.demo.repository;

import com.example.demo.model.Document;
import com.example.demo.model.DocumentChunk;
import com.example.demo.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
  List<Document> findByOrganizationId(Long organizationId);
  List<Document> findByOrganization (Organization org);

  List<Document> findByOrganization_WhatsappNumber(String whatsappNumber);
  List<DocumentChunk> findByDocument_Id(Long documentId);

}
