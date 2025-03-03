package com.gatcha.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class GatchaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatchaApiApplication.class, args);
    }
} 