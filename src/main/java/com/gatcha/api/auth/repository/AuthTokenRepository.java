package com.gatcha.api.auth.repository;

import com.gatcha.api.auth.model.AuthToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends MongoRepository<AuthToken, String> {
    Optional<AuthToken> findByToken(String token);

    Optional<AuthToken> findByUsername(String username);
}