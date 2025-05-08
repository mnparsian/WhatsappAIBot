package com.example.demo.service;

import com.example.demo.model.MessageLog;
import com.example.demo.model.Organization;
import com.example.demo.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;

    public void logMessage(boolean fromUser, String text, Organization org) {
        MessageLog log = new MessageLog();
        log.setFromUser(fromUser);
        log.setMessageText(text);
        log.setTimestamp(LocalDateTime.now());
        log.setOrganization(org);
        messageLogRepository.save(log);
    }

    public List<MessageLog> getMessagesByWhatsappNumber(String whatsappNumber) {
        return messageLogRepository.findByOrganization_WhatsappNumberOrderByTimestampDesc(whatsappNumber);
    }
}
