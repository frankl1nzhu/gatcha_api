package com.gatcha.api.service;

import com.gatcha.api.dto.SummonResponse;
import com.gatcha.api.model.Monster;
import com.gatcha.api.model.MonsterTemplate;
import com.gatcha.api.model.SummonRecord;
import com.gatcha.api.repository.MonsterRepository;
import com.gatcha.api.repository.MonsterTemplateRepository;
import com.gatcha.api.repository.SummonRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummonService {
    private final MonsterTemplateRepository monsterTemplateRepository;
    private final MonsterRepository monsterRepository;
    private final SummonRecordRepository summonRecordRepository;
    private final PlayerService playerService;
    private final Random random = new Random();

    @Transactional
    public SummonResponse summonMonster(String playerId) {
        // Check if player has reached max monsters
        long currentMonsterCount = monsterRepository.countByPlayerId(playerId);
        if (currentMonsterCount >= 10) { // Assuming max monsters is 10
            throw new IllegalStateException("Player has reached maximum monster capacity");
        }

        // Get all monster templates sorted by summon rate
        List<MonsterTemplate> templates = monsterTemplateRepository.findAllByOrderBySummonRateDesc();
        if (templates.isEmpty()) {
            throw new IllegalStateException("No monster templates available");
        }

        // Calculate total probability
        double totalRate = templates.stream()
                .mapToDouble(MonsterTemplate::getSummonRate)
                .sum();

        // Randomly select a monster template
        double randomValue = random.nextDouble() * totalRate;
        double currentSum = 0;
        MonsterTemplate selectedTemplate = templates.get(0);

        for (MonsterTemplate template : templates) {
            currentSum += template.getSummonRate();
            if (randomValue <= currentSum) {
                selectedTemplate = template;
                break;
            }
        }

        // Create summon record
        SummonRecord record = new SummonRecord();
        record.setPlayerId(playerId);
        record.setTemplateId(selectedTemplate.getId());
        record.setMonsterName(selectedTemplate.getName());
        record.setElementType(selectedTemplate.getElementType());
        record = summonRecordRepository.save(record);

        // Create monster instance
        Monster monster = selectedTemplate.createMonster(playerId);
        monster = monsterRepository.save(monster);

        // Update summon record
        record.setMonsterId(monster.getId());
        record.setProcessed(true);
        record.setProcessedAt(System.currentTimeMillis());
        record = summonRecordRepository.save(record);

        // Add monster to player's list
        playerService.addMonster(playerId, monster.getId());

        return convertToResponse(record);
    }

    public List<SummonResponse> getPlayerSummons(String playerId) {
        return summonRecordRepository.findByPlayerIdOrderByCreatedAtDesc(playerId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedDelay = 60000) // Execute every minute
    @Transactional
    public void processUnprocessedSummons() {
        List<SummonRecord> unprocessedRecords = summonRecordRepository.findByIsProcessedFalseOrderByCreatedAtAsc();

        for (SummonRecord record : unprocessedRecords) {
            try {
                MonsterTemplate template = monsterTemplateRepository.findById(record.getTemplateId())
                        .orElseThrow(() -> new IllegalStateException("Template not found"));

                Monster monster = template.createMonster(record.getPlayerId());
                monster = monsterRepository.save(monster);

                record.setMonsterId(monster.getId());
                record.setProcessed(true);
                record.setProcessedAt(System.currentTimeMillis());
                summonRecordRepository.save(record);

                playerService.addMonster(record.getPlayerId(), monster.getId());
            } catch (Exception e) {
                // Log error but continue processing other records
                e.printStackTrace();
            }
        }
    }

    private SummonResponse convertToResponse(SummonRecord record) {
        SummonResponse response = new SummonResponse();
        response.setSummonId(record.getId());
        response.setMonsterId(record.getMonsterId());
        response.setMonsterName(record.getMonsterName());
        response.setElementType(record.getElementType());
        response.setSummonedAt(record.getCreatedAt());
        return response;
    }
}