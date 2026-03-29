package org.example.learnlink.modules.email.listener;


import jakarta.mail.MessagingException;
import org.example.learnlink.modules.auth.event.OnUserRegisteredEvent;
import org.example.learnlink.modules.email.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for user registration events and sends verification emails.
 * Async to avoid blocking the registration process.
 */
@Component
public class UserRegistrationListener {

    private final EmailService emailService;

    public UserRegistrationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handleUserRegistered(OnUserRegisteredEvent event)throws MessagingException {
        emailService.sendVerificationEmail(event.getUser()) ;
    }
}
