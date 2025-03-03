package com.gatcha.api.repository;

import com.gatcha.api.model.AuthToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface AuthTokenRepository extends MongoRepository<AuthToken, String> {
    Optional<AuthToken> findByToken(String token);
}