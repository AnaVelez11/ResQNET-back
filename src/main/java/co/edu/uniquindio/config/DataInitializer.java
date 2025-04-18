package co.edu.uniquindio.config;

import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            log.info("Verificando datos iniciales...");

            if (userRepository.count() == 0) {
                log.info("No se encontraron usuarios. Creando datos iniciales...");

                User admin1 = User.builder()
                        .id("678")
                        .name("Admin Uno")
                        .email("admin1@uniquindio.edu.co")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .phone("1234567890")
                        .address("Calle 123")
                        .city("Ciudad Uniquindio")
                        .birthDate(new Date())
                        .location(new GeoJsonPoint(-75.677, 4.534))
                        .reports(List.of())
                        .activationCodes(List.of())
                        .resetCodes(List.of())
                        .build();

                User admin2 = User.builder()
                        .id("912")
                        .name("Admin Dos")
                        .email("admin2@uniquindio.edu.co")
                        .password(passwordEncoder.encode("admin456"))
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .phone("0987654321")
                        .address("Carrera 456")
                        .city("Ciudad Dos")
                        .birthDate(new Date())
                        .location(new GeoJsonPoint(-74.060, 4.710))
                        .reports(List.of())
                        .activationCodes(List.of())
                        .resetCodes(List.of())
                        .build();

                userRepository.saveAll(List.of(admin1, admin2));
                log.info("Usuarios admin creados exitosamente!");
            } else {
                log.info("Ya existen usuarios en la base de datos. No se crearon datos iniciales.");
            }
        } catch (Exception e) {
            log.error("Error al inicializar datos: ", e);
        }
    }
}