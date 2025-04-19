package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.model.ActivationCode;
import co.edu.uniquindio.model.ResetCode;
import co.edu.uniquindio.services.interfaces.EmailService;
import co.edu.uniquindio.dto.LoginDTO;
import co.edu.uniquindio.exceptions.AuthException;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    @Override
    public String login(LoginDTO loginDTO) {
        Optional<User> optionalUser = userRepository.findByEmail(loginDTO.email());

        if (optionalUser.isEmpty()) {
            throw new AuthException("Usuario no encontrado.");
        }

        User user = optionalUser.get();

        // Si el usuario no está activado
        if (user.getStatus() != UserStatus.ACTIVE) {
            // Buscar un código de activación válido
            Optional<ActivationCode> optionalCode = user.getActivationCodes().stream()
                    .filter(ac -> !ac.isUsed() && ac.getExpirationDate().after(new Date()))
                    .findFirst();

            String activationCode;

            if (optionalCode.isPresent()) {
                activationCode = optionalCode.get().getCode();
            } else {
                // Generar uno nuevo si no existe un código válido
                activationCode = UUID.randomUUID().toString().substring(0, 6);

                ActivationCode newCode = ActivationCode.builder()
                        .creationDate(new Date())
                        .expirationDate(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minutos
                        .code(activationCode)
                        .used(false)
                        .build();

                if (user.getActivationCodes() == null) {
                    user.setActivationCodes(new ArrayList<>());
                }

                user.getActivationCodes().add(newCode);
                userRepository.save(user);
            }

            // Reenviar el correo con el código (válido o nuevo)
            emailService.sendActivationEmail(user.getEmail(), activationCode);

            throw new AuthException("La cuenta no está activada. Se ha enviado un código de activación al correo.");
        }

        if (!passwordEncoder.matches(loginDTO.password(), user.getPassword())) {
            throw new AuthException("Credenciales incorrectas.");
        }

        return "Inicio de sesión exitoso. Bienvenid@, " + user.getName() + "!";
    }

    @Override
    public boolean activateUser(String email, String code) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new ValueConflictException("Usuario no encontrado");
        }

        User user = optionalUser.get();
        ActivationCode activation = user.getActivationCodes().stream()
                .filter(ac -> ac.getCode().equals(code) && !ac.isUsed())
                .findFirst()
                .orElseThrow(() -> new ValueConflictException("Código inválido o expirado"));

        activation.setUsed(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        return true;
    }

    @Override
    public void sendPasswordResetCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new AuthException("No existe un usuario con ese correo.");
        }

        User user = optionalUser.get();

        // Generar código
        String code = UUID.randomUUID().toString().substring(0, 6);

        // Crear objeto ResetCode
        Date now = new Date();
        ResetCode resetCode = ResetCode.builder()
                .code(code)
                .creationDate(now)
                .expirationDate(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .used(false)
                .build();

        // Asegurar que la lista exista
        if (user.getResetCodes() == null) {
            user.setResetCodes(new ArrayList<>());
        }

        // Guardar el código
        user.getResetCodes().add(resetCode);
        userRepository.save(user);

        // Enviar por correo
        emailService.sendPasswordResetEmail(user.getEmail(), code);
    }

    @Override
    public boolean resetPassword(String email, String code, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new AuthException("Usuario no encontrado.");
        }
        User user = optionalUser.get();

        ResetCode validCode = user.getResetCodes().stream()
                .filter(rc -> rc.getCode().equals(code) && !rc.isUsed() && rc.getExpirationDate().after(new Date()))
                .findFirst()
                .orElseThrow(() -> new AuthException("Código inválido o expirado."));

        // Marcar código como usado
        validCode.setUsed(true);

        // Actualizar la contraseña
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        return true;
    }
}
