package com.example.demo.service;

import com.example.demo.model.Organization;
import com.example.demo.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {
  @Autowired private OrganizationRepository organizationRepository;

  public Organization getOrganizationByWhatsappNumber(String whatsappNumber) {
    System.out.println("Searching for whatsapp number: " + whatsappNumber);  // Log
    return organizationRepository
            .findByWhatsappNumberNative(whatsappNumber)
            .orElseThrow(() -> new IllegalArgumentException("No organization found for the WhatsApp number: " + whatsappNumber));
  }

  public List<Organization> getAllOrganizations() {
    return organizationRepository.findAll();
  }

}
