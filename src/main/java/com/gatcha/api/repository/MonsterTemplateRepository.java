package com.gatcha.api.repository;

import com.gatcha.api.model.MonsterTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MonsterTemplateRepository extends MongoRepository<MonsterTemplate, String> {
    List<MonsterTemplate> findAllByOrderBySummonRateDesc();
}