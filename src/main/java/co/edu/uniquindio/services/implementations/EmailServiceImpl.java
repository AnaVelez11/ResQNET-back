package co.edu.uniquindio.services.implementations;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import co.edu.uniquindio.services.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;


    @Override
    public void sendActivationEmail(String to, String activationCode) {
        String subject = "Activación de cuenta - ResQNET";
        String body = "Hola,\n\nTu código de activación es: " + activationCode +
                "\n\nIngresa este código en la plataforma para completar tu registro.\n\nSaludos,\nResQNET";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.setFrom("anamariav749@gmail.com"); // Remitente

            mailSender.send(message);
            logger.info("Correo de activación enviado a {}", to);
        } catch (MessagingException e) {
            logger.error("Error al enviar el correo a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage());
        }
    }
    @Override
    public void sendPasswordResetEmail(String to, String code) {
        String subject = "Recuperación de contraseña - ResQNET";
        String body = "Hola,\n\n" +
                "Has solicitado restablecer tu contraseña.\n" +
                "Tu código de recuperación es: " + code + "\n\n" +
                "Este código es válido por 15 minutos.\n\n" +
                "Si no solicitaste este cambio, ignora este mensaje.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de recuperación: " + e.getMessage());
        }
    }
    @Override
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException e) {
            throw new MessagingException("Error enviando email: " + e.getMessage());
        }
    }







}
