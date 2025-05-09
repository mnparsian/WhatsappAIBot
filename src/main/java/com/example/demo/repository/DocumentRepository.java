package com.example.demo.repository;

import com.example.demo.model.Document;
import com.example.demo.model.DocumentChunk;
import com.example.demo.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
  List<Document> findByOrganizationId(Long organizationId);
  List<Document> findByOrganization (Organization org);

  List<Document> findByOrganization_WhatsappNumber(String whatsappNumber);
  Optional<Document> findById(Long id);


}
