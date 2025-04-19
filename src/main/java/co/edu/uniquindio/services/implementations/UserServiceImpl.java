package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.mappers.UserMapper;
import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.model.enums.ReportStatus;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ReportRepository reportRepository;

    @Override
    public UserResponse createUser(UserRegistrationRequest request) {
        // Verificar si el email ya está en uso
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ValueConflictException("Email ya registrado");
        }

        // Validación básica de coordenadas
        if (Math.abs(request.longitude()) > 180 || Math.abs(request.latitude()) > 90) {
            throw new IllegalArgumentException("Coordenadas inválidas");
        }

        // Crear el punto GeoJSON con las coordenadas
        GeoJsonPoint location = new GeoJsonPoint(request.longitude(), request.latitude());

        // Construcción del usuario con los atributos correctos
        User newUser = User.builder().id(request.id()).name(request.fullName()).email(request.email()).password(encode(request.password())) //Encriptar contraseña
                .phone(request.phone()).address(request.address()).city(request.city()).birthDate(java.sql.Date.valueOf(request.dateBirth())) //Convertir LocalDate a Date
                .role(Role.CLIENT).status(UserStatus.REGISTERED).activationCodes(new ArrayList<>()) // Lista vacía de códigos de activación
                .reports(new ArrayList<>()) // Lista vacía de reportes
                .location(location).build();


        // Guardar el usuario en MongoDB
        newUser = userRepository.save(newUser);

        // Retornar la respuesta mapeada
        return userMapper.toUserResponse(newUser);
    }

    private String encode(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Optional<UserResponse> getUser(String id) {
        return userRepository.findById(id).map(userMapper::toUserResponse);
    }

    @Transactional
    public void deactivateUser(String userId) {
        // 1. Validar y desactivar usuario
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!user.isActive()) {
            log.warn("El usuario {} ya estaba desactivado", userId);
            return;
        }

        user.setActive(false);
        user.setStatus(UserStatus.INACTIVE);
        user.setDeactivationDate(LocalDateTime.now()); // Nuevo campo para auditoría
        userRepository.save(user);

        // 2. Anonimizar reportes (con verificación)
        List<Report> userReports = reportRepository.findByIdUser(userId);

        if (!userReports.isEmpty()) {
            userReports.forEach(report -> {
                if (!report.isAnonymous()) { // Solo actualizar si no están anónimos
                    report.setAnonymous(true);
                    report.setStatus(ReportStatus.ANONYMOUS);
                    report.setIdUser("ANONYMOUS");
                }
            });
            reportRepository.saveAll(userReports);
            log.info("{} reportes anonimizados para el usuario {}", userReports.size(), userId);
        } else {
            log.info("El usuario {} no tenía reportes para anonimizar", userId);
        }
    }

}
