package com.example.demo.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendMessage(String toNumber, String message) {
        String cleanedNumber = toNumber.startsWith("whatsapp:")
                ? toNumber.replace("whatsapp:", "")
                : toNumber;
        cleanedNumber = cleanedNumber.replaceAll("[^\\d+]", "");
        System.out.println("Final toNumber = " + cleanedNumber);

        System.out.println("🚀 Sending WhatsApp message to: " + cleanedNumber);
        System.out.println("📨 Message content: " + message);
        try {
            Message.creator(
                    new PhoneNumber("whatsapp:" + cleanedNumber),
                    new PhoneNumber("whatsapp:" + fromNumber),
                    message
            ).create();
            System.out.println("✅ Message sent successfully.");
        } catch (Exception e) {
            System.err.println("❌ Failed to send WhatsApp message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
