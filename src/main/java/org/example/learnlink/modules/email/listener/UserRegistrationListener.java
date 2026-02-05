package org.example.learnlink.modules.email.listener;


import org.example.learnlink.modules.auth.event.OnUserRegisteredEvent;
import org.example.learnlink.modules.email.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationListener {
    private final EmailService emailService;

    public UserRegistrationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    public void handleUserRegistered(OnUserRegisteredEvent event) {
        emailService.sendVerificationEmail(event.getUser());
    }
}
