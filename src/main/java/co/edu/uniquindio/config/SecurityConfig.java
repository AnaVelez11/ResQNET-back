package co.edu.uniquindio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)

public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfig(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //Deshabilitar CSRF
                .csrf(csrf -> csrf.disable())

                //Configurar CORS (permite conexiones desde cualquier origen)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("*")); // Permite todos los orígenes (ajusta en producción)
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))
                //Definir reglas de acceso para endpoints:
                .authorizeHttpRequests(auth -> auth
                        // Endpoints que requieren rol ADMIN o CLIENT:
                        .requestMatchers(
                                "/api/reports/create",
                                "/api/reports/update/**",
                                "/api/reports/delete/**",
                                "/api/categories/create",
                                "/api/categories/**"
                        ).hasAnyAuthority("ADMIN", "CLIENT")
                        .anyRequest().permitAll()
                )
                //Añadir filtro JWT antes de la autenticación básica
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    protected MethodSecurityExpressionHandler expressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setDefaultRolePrefix("");
        return handler;
    }
}