package org.example.learnlink.modules.email.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.auth.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {


    @Value("${app.url}")
    private String appUrl;
    private final JavaMailSender mailSender;


    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(User user) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject("Verify your email");

        // HTML content
        String html = "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<h2>Welcome to LearnLink!</h2>" +
                "<p>Click the link below to activate your account:</p>" +
                "<p style='color: #1a73e8;'>Activate Account</p>" +
                "<p>Or copy the code below and paste it in our website:</p>" +
                "<div style='background: #f4f4f4; padding: 10px; border-radius: 5px; font-family: monospace; user-select: all;'>" +
                user.getVerificationCode() +
                "</div>" +
                "<p>Thank you!</p>" +
                "</body>" +
                "</html>";

        helper.setText(html, true); // true = HTML

        mailSender.send(message);
    }
}
