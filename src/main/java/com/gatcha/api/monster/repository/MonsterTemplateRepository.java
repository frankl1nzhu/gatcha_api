package com.gatcha.api.monster.repository;

import com.gatcha.api.monster.model.MonsterTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonsterTemplateRepository extends MongoRepository<MonsterTemplate, Integer> {
    Optional<MonsterTemplate> findById(Integer id);
}