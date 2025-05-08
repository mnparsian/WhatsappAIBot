package com.example.demo.repository;

import com.example.demo.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
  @Query(value = "SELECT * FROM organization WHERE whatsapp_number = :whatsappNumber", nativeQuery = true)
  Optional<Organization> findByWhatsappNumber(@Param("whatsappNumber") String whatsappNumber);

  @Query(value = "SELECT * FROM organization WHERE whatsapp_number = :whatsappNumber", nativeQuery = true)
  Optional<Organization> findByWhatsappNumberNative(@Param("whatsappNumber") String whatsappNumber);



}
