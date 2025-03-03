package com.gatcha.api.repository;

import com.gatcha.api.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PlayerRepository extends MongoRepository<Player, String> {
    Optional<Player> findByUserId(String userId);

    Optional<Player> findByUsername(String username);
}