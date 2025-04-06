package co.edu.uniquindio.config;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MongoConnectionChecker {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void checkConnection() {
        System.out.println("Collections disponibles: " + mongoTemplate.getCollectionNames());
    }
}
