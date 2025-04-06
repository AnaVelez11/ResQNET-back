package co.edu.uniquindio.services.interfaces;

public interface EmailService {
    void sendActivationEmail(String to, String activationCode);
    void sendPasswordResetEmail(String to, String code);


}
