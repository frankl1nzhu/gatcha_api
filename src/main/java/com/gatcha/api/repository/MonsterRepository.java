package com.gatcha.api.repository;

import com.gatcha.api.model.Monster;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface MonsterRepository extends MongoRepository<Monster, String> {
    List<Monster> findByPlayerId(String playerId);

    Optional<Monster> findByIdAndPlayerId(String id, String playerId);

    long countByPlayerId(String playerId);
}