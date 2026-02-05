package org.example.learnlink.modules.email.service;


import org.example.learnlink.modules.auth.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {


    @Value("${app.url}")
    private String appUrl;
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(User user) {
        String verifyLink = appUrl + "/api/auth/verify?code=" + user.getVerificationCode();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify your email");
        message.setText("Click the link to activate your account: " + verifyLink);
        mailSender.send(message);
    }


}
