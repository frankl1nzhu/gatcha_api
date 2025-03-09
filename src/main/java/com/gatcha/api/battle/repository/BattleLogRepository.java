package com.gatcha.api.battle.repository;

import com.gatcha.api.battle.model.BattleLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleLogRepository extends MongoRepository<BattleLog, String> {
    List<BattleLog> findByMonster1IdOrMonster2Id(String monster1Id, String monster2Id);

    Optional<BattleLog> findById(String id);
}