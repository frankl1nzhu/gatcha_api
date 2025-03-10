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

import java.util.ArrayList;
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
    public List<PlayerMonster> summonMultiple(String username, int count) {
        // Limit maximum summon count to 10
        count = Math.min(count, 10);

        System.out.println("Starting multiple summons for user " + username + ", requested count: " + count);

        // Create a list to store summoned monsters
        List<PlayerMonster> summonedMonsters = new ArrayList<>();

        // Get player's current monster count and maximum monster count
        int currentMonsterCount = playerService.getMonsters(username).size();
        int maxMonsterCount = playerService.getProfile(username).getMaxMonsters();

        // Calculate how many monsters can be summoned
        int availableSlots = maxMonsterCount - currentMonsterCount;
        int actualCount = Math.min(count, availableSlots);

        System.out.println("User " + username + " current monster count: " + currentMonsterCount +
                ", maximum monster count: " + maxMonsterCount +
                ", available slots: " + availableSlots +
                ", actual summon count: " + actualCount);

        // If there are no available slots, throw an exception
        if (actualCount <= 0) {
            System.out.println("User " + username + " has reached the monster limit, cannot summon");
            throw new RuntimeException(
                    "You have reached your monster limit. Please level up or remove some monsters first");
        }

        // Perform multiple summons
        for (int i = 0; i < actualCount; i++) {
            try {
                System.out.println("Performing summon " + (i + 1) + " for user " + username);
                PlayerMonster monster = summon(username);
                summonedMonsters.add(monster);
                System.out.println("Summon " + (i + 1) + " successful, monster ID: " + monster.getId());
            } catch (Exception e) {
                // If summon fails, log the error but continue trying
                System.out.println("Summon " + (i + 1) + " failed: " + e.getMessage());
            }
        }

        // If no monsters were successfully summoned, throw an exception
        if (summonedMonsters.isEmpty()) {
            System.out.println("All summons failed for user " + username);
            throw new RuntimeException("All summons failed, please try again later");
        }

        System.out.println("User " + username + " successfully summoned " + summonedMonsters.size() + " monsters");
        return summonedMonsters;
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