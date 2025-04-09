package com.example.demo.data;

import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.repositories.UserRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TestDataLoader {

    public static Map<String, User> loadTestData(UserRepository userRespository, MongoTemplate mongoTemplate) {
        var encoder = new BCryptPasswordEncoder();
        return loadTestData(
                List.of(
//new User(UUID.randomUUID().toString(),)
                ),
                userRespository,
                mongoTemplate
        );
    }

    public static Map<String, User> loadTestData(Collection<User> newUsers,UserRepository userRespository, MongoTemplate mongoTemplate) {
        // Borrar datos existentes para asegurar la repetibilidad de las pruebas.
        mongoTemplate.getDb().listCollectionNames()
                .forEach(mongoTemplate::dropCollection);
        return userRespository.saveAll(newUsers).stream().collect(Collectors.toMap(User::getId, usuario -> usuario));
    }
}
