package co.edu.uniquindio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // Habilitar un broker simple (en memoria) para enviar mensajes a estos prefijos
        config.enableSimpleBroker("/topic", "/queue");

        // Prefijo para los mensajes que llegan AL servidor (desde el cliente)

        config.setApplicationDestinationPrefixes("/app");

        // Prefijo para mensajes dirigidos a usuarios específicos (messaging privado)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // Definir el endpoint para conectarse via WebSocket
        registry.addEndpoint("/ws")

                // Permite conexiones desde cualquier origen
                .setAllowedOriginPatterns("*")

                // Habilitar fallback a SockJS (HTTP long-polling) si WebSocket no está disponible
                .withSockJS();
    }

}