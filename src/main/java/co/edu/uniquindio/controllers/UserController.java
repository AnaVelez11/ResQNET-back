package co.edu.uniquindio.controllers;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRegistrationRequest request) {
        // Llamamos al servicio para crear el usuario
        UserResponse response = userService.createUser(request);

        // Construimos la URL del usuario creado
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable("id") String id) {
        Optional<UserResponse> userResponse = userService.getUser(id);

        return userResponse
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }




}