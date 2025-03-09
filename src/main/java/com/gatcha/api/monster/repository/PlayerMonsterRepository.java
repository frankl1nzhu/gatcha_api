package com.gatcha.api.monster.repository;

import com.gatcha.api.monster.model.PlayerMonster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerMonsterRepository extends MongoRepository<PlayerMonster, String> {
    List<PlayerMonster> findByUsername(String username);

    Optional<PlayerMonster> findByIdAndUsername(String id, String username);
}