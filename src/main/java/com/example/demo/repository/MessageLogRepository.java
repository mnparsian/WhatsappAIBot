package com.example.demo.repository;

import com.example.demo.model.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageLogRepository extends JpaRepository<MessageLog,Long> {
    List<MessageLog> findByOrganization_WhatsappNumberOrderByTimestampDesc(String whatsappNumber);

}
