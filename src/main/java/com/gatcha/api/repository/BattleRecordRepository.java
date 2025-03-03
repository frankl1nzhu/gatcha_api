package com.gatcha.api.repository;

import com.gatcha.api.model.BattleRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BattleRecordRepository extends MongoRepository<BattleRecord, String> {
    List<BattleRecord> findByPlayer1IdOrPlayer2IdOrderByStartTimeDesc(String player1Id, String player2Id);
}