package co.edu.uniquindio.controllers;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /// / Registrar un nuevo usuario en el sistema
    ///
    /// / Retorna respuesta con datos del usuario y URL de ubicación (status 201)
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRegistrationRequest request) {
        // Llamamos al servicio para crear el usuario
        var response = userService.createUser(request);
        // Construimos la URL del usuario creado
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /// / Obtener información de un usuario específico por ID
    ///
    /// / Retorna datos del usuario (200) o 404 si no existe
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable("id") String id) {
        Optional<UserResponse> userResponse = userService.getUser(id);

        return userResponse
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /// / Desactivar cuenta de usuario (Admin o propio usuario)
    ///
    /// / Retorna confirmación de desactivación (200)
    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<String> deactivateUser(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader) {

        userService.deactivateUser(userId);
        return ResponseEntity.ok("Cuenta desactivada y reportes anonimizados");
    }
}