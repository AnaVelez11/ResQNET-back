package co.edu.uniquindio.services.interfaces;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendActivationEmail(String to, String activationCode);

    void sendPasswordResetEmail(String to, String code);

    void sendEmail(String to, String subject, String body) throws MessagingException;


}
