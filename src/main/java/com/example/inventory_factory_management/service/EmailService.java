package com.example.inventory_factory_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String toEmail, String subject, String message) {
        System.out.println("=== EMAIL SENDING ATTEMPT ===");
        System.out.println("From: " + fromEmail);
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            System.out.println("Attempting to send email via JavaMailSender...");
            javaMailSender.send(mailMessage);

            System.out.println("EMAIL SENT SUCCESSFULLY to: " + toEmail);

        } catch (Exception e) {
            System.err.println("FAILED to send email to " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}