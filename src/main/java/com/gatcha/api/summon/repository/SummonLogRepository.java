package com.gatcha.api.summon.repository;

import com.gatcha.api.summon.model.SummonLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SummonLogRepository extends MongoRepository<SummonLog, String> {
    List<SummonLog> findByProcessed(boolean processed);

    List<SummonLog> findByUsername(String username);
}