package co.edu.uniquindio.controllers;

import co.edu.uniquindio.dto.ActivateUserRequest;
import co.edu.uniquindio.dto.LoginDTO;
import co.edu.uniquindio.dto.PasswordResetRequest;
import co.edu.uniquindio.model.ActivationCode;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.AuthService;
import co.edu.uniquindio.services.interfaces.EmailService;
import co.edu.uniquindio.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    /// /Autenticar al usuario (email + contraseña)
    ///
    /// Retorna token JWT si las credenciales son válidas
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        String token = authService.login(loginDTO);
        return ResponseEntity.ok(token);
    }

    /// / Activar cuenta de usuario con código de verificación
    ///
    /// / Retorna confirmación si el código es válido, error si no
    @PostMapping("/activate")
    public ResponseEntity<String> activateUser(@RequestBody @Validated ActivateUserRequest request) {
        boolean activated = authService.activateUser(request.email(), request.code());

        if (activated) {
            return ResponseEntity.ok("Usuario activado correctamente");
        } else {
            return ResponseEntity.badRequest().body("No se pudo activar el usuario");
        }
    }

    /// / Enviar código de activación al email del usuario
    ///
    /// / Retorna confirmación de envío o error si el email no existe
    @PostMapping("/send-activation-email")
    public ResponseEntity<String> sendActivationEmail(@RequestParam String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        User user = optionalUser.get();

        // Generar código de activación de 6 caracteres
        String activationCode = UUID.randomUUID().toString().substring(0, 6);

        // Crear y configurar el código de activación
        ActivationCode activation = ActivationCode.builder()
                .creationDate(new Date())
                .code(activationCode)
                .expirationDate(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minutos de validez
                .used(false)
                .build();

        // Verificar si la lista de códigos de activación es nula
        if (user.getActivationCodes() == null) {
            user.setActivationCodes(new ArrayList<>()); // Inicializar la lista
        }

        user.getActivationCodes().add(activation); // Agregar el código generado
        userRepository.save(user); // Guardar los cambios en la BD

        // Enviar correo de activación
        emailService.sendActivationEmail(email, activationCode);

        return ResponseEntity.ok("Correo de activación enviado a " + email);
    }

    /// / Solicitar reseteo de contraseña (envía código al email)
    ///
    /// / Retorna confirmación de envío
    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestReset(@RequestParam String email) {
        authService.sendPasswordResetCode(email);
        return ResponseEntity.ok("Se ha enviado un código de recuperación al correo.");
    }

    /// / Restablecer contraseña con código de verificación
    ///
    /// / Retorna éxito o error si el código/email son inválidos
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        boolean result = authService.resetPassword(request.email(), request.code(), request.newPassword());
        return result
                ? ResponseEntity.ok("Contraseña restablecida con éxito.")
                : ResponseEntity.badRequest().body("No se pudo restablecer la contraseña.");
    }

    /// / Generar token JWT de prueba (solo para desarrollo)
    ///
    /// / Retorna token generado manualmente
    @GetMapping("/generate-token/{userId}")
    public String generateToken(@PathVariable String userId, String role) {
        return jwtUtil.generateTestToken(userId, role);

    }


}
