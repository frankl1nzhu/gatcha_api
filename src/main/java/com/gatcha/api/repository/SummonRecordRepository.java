package com.gatcha.api.repository;

import com.gatcha.api.model.SummonRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SummonRecordRepository extends MongoRepository<SummonRecord, String> {
    List<SummonRecord> findByPlayerIdOrderByCreatedAtDesc(String playerId);

    List<SummonRecord> findByIsProcessedFalseOrderByCreatedAtAsc();
}