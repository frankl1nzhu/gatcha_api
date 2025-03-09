package com.gatcha.api.summon.service.impl;

import com.gatcha.api.monster.model.MonsterTemplate;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.repository.MonsterTemplateRepository;
import com.gatcha.api.monster.service.MonsterService;
import com.gatcha.api.player.service.PlayerService;
import com.gatcha.api.summon.model.SummonLog;
import com.gatcha.api.summon.repository.SummonLogRepository;
import com.gatcha.api.summon.service.SummonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class SummonServiceImpl implements SummonService {

    private final MonsterTemplateRepository monsterTemplateRepository;
    private final SummonLogRepository summonLogRepository;
    private final MonsterService monsterService;
    private final PlayerService playerService;
    private final Random random = new Random();

    public SummonServiceImpl(MonsterTemplateRepository monsterTemplateRepository,
            SummonLogRepository summonLogRepository,
            MonsterService monsterService,
            PlayerService playerService) {
        this.monsterTemplateRepository = monsterTemplateRepository;
        this.summonLogRepository = summonLogRepository;
        this.monsterService = monsterService;
        this.playerService = playerService;
    }

    @Override
    public PlayerMonster summon(String username) {
        // Get all monster templates
        List<MonsterTemplate> templates = monsterTemplateRepository.findAll();

        // Calculate total probability
        double totalRate = templates.stream()
                .mapToDouble(MonsterTemplate::getLootRate)
                .sum();

        // Randomly select a monster
        double randomValue = random.nextDouble() * totalRate;
        double cumulativeRate = 0.0;
        MonsterTemplate selectedTemplate = null;

        for (MonsterTemplate template : templates) {
            cumulativeRate += template.getLootRate();
            if (randomValue <= cumulativeRate) {
                selectedTemplate = template;
                break;
            }
        }

        if (selectedTemplate == null) {
            selectedTemplate = templates.get(0); // Default to the first one
        }

        // Record summon log, convert ID to string for storage
        SummonLog summonLog = new SummonLog(username, String.valueOf(selectedTemplate.getId()));
        summonLogRepository.save(summonLog);

        try {
            // Create monster, passing integer ID
            PlayerMonster monster = monsterService.createMonsterFromTemplate(selectedTemplate.getId(), username);

            // Add monster to player's list
            boolean added = playerService.addMonster(username, monster.getId());

            if (added) {
                // Update summon log
                summonLog.setMonsterId(monster.getId());
                summonLog.setProcessed(true);
                summonLogRepository.save(summonLog);
                return monster;
            } else {
                throw new RuntimeException("Failed to add monster to player's list");
            }
        } catch (Exception e) {
            // Record failure, but don't rethrow exception
            summonLog.setProcessed(false);
            summonLogRepository.save(summonLog);
            throw new RuntimeException("Summon failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SummonLog> getSummonHistory(String username) {
        return summonLogRepository.findByUsername(username);
    }

    @Override
    public void reprocessFailedSummons() {
        List<SummonLog> failedSummons = summonLogRepository.findByProcessed(false);

        for (SummonLog summonLog : failedSummons) {
            try {
                // Create monster, convert string ID to integer
                Integer templateId = Integer.parseInt(summonLog.getTemplateId());
                PlayerMonster monster = monsterService.createMonsterFromTemplate(templateId, summonLog.getUsername());

                // Add monster to player's list
                boolean added = playerService.addMonster(summonLog.getUsername(), monster.getId());

                if (added) {
                    // Update summon log
                    summonLog.setMonsterId(monster.getId());
                    summonLog.setProcessed(true);
                    summonLogRepository.save(summonLog);
                }
            } catch (Exception e) {
                // Record failure, but continue processing the next one
                System.err.println("Failed to reprocess summon: " + summonLog.getId() + ", error: " + e.getMessage());
            }
        }
    }
}