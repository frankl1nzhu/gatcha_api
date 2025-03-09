package com.gatcha.api.battle.service.impl;

import com.gatcha.api.battle.model.BattleLog;
import com.gatcha.api.battle.repository.BattleLogRepository;
import com.gatcha.api.battle.service.BattleService;
import com.gatcha.api.monster.model.PlayerMonster;
import com.gatcha.api.monster.model.Skill;
import com.gatcha.api.monster.service.MonsterService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BattleServiceImpl implements BattleService {

    private final BattleLogRepository battleLogRepository;
    private final MonsterService monsterService;
    // Store the experience gained from the most recent battle
    private final Map<String, Integer> battleExperienceGained = new HashMap<>();

    public BattleServiceImpl(BattleLogRepository battleLogRepository, MonsterService monsterService) {
        this.battleLogRepository = battleLogRepository;
        this.monsterService = monsterService;
    }

    @Override
    public BattleLog battle(String monster1Id, String monster2Id, String username) {
        PlayerMonster monster1 = monsterService.getMonsterById(monster1Id, username);
        PlayerMonster monster2 = monsterService.getMonsterById(monster2Id, username);

        // Create battle log
        BattleLog battleLog = new BattleLog();
        battleLog.setMonster1Id(monster1Id);
        battleLog.setMonster2Id(monster2Id);
        battleLog.setBattleDate(new Date());
        battleLog.setActions(new ArrayList<>());

        // Copy monster attributes for battle
        int hp1 = monster1.getHp();
        int hp2 = monster2.getHp();

        // Determine who attacks first based on speed
        boolean monster1First = monster1.getVit() >= monster2.getVit();

        // Skill cooldown counters
        Map<Integer, Integer> cooldowns1 = initCooldowns(monster1);
        Map<Integer, Integer> cooldowns2 = initCooldowns(monster2);

        // Battle loop
        while (hp1 > 0 && hp2 > 0) {
            if (monster1First) {
                // Monster 1 attacks
                hp2 = performAttack(monster1, monster2, hp1, hp2, cooldowns1, battleLog, true);
                if (hp2 <= 0)
                    break;

                // Monster 2 attacks
                hp1 = performAttack(monster2, monster1, hp2, hp1, cooldowns2, battleLog, false);
            } else {
                // Monster 2 attacks
                hp1 = performAttack(monster2, monster1, hp2, hp1, cooldowns2, battleLog, false);
                if (hp1 <= 0)
                    break;

                // Monster 1 attacks
                hp2 = performAttack(monster1, monster2, hp1, hp2, cooldowns1, battleLog, true);
            }

            // Update cooldowns
            updateCooldowns(cooldowns1);
            updateCooldowns(cooldowns2);
        }

        // Set the winner
        String winnerId = hp1 > 0 ? monster1Id : monster2Id;
        battleLog.setWinnerId(winnerId);

        // Add experience to the winning monster
        // Calculate experience gained: base experience + defeated monster level * 10
        PlayerMonster winner = hp1 > 0 ? monster1 : monster2;
        PlayerMonster loser = hp1 > 0 ? monster2 : monster1;
        int expGained = 20 + (loser.getLevel() * 10);

        // Save battle log
        BattleLog savedBattleLog = battleLogRepository.save(battleLog);

        // Call MonsterService's addExperience method to add experience to the winning
        // monster
        monsterService.addExperience(winnerId, username, expGained);

        // Store the experience gained from this battle
        battleExperienceGained.put(savedBattleLog.getId(), expGained);

        return savedBattleLog;
    }

    @Override
    public BattleLog getBattleById(String battleId) {
        return battleLogRepository.findById(battleId)
                .orElseThrow(() -> new RuntimeException("Battle not found"));
    }

    @Override
    public List<BattleLog> getBattlesByMonsterId(String monsterId) {
        return battleLogRepository.findByMonster1IdOrMonster2Id(monsterId, monsterId);
    }

    @Override
    public int getExperienceGained(String battleId) {
        return battleExperienceGained.getOrDefault(battleId, 0);
    }

    private Map<Integer, Integer> initCooldowns(PlayerMonster monster) {
        Map<Integer, Integer> cooldowns = new HashMap<>();
        for (Skill skill : monster.getSkills()) {
            cooldowns.put(skill.getNum(), 0);
        }
        return cooldowns;
    }

    private void updateCooldowns(Map<Integer, Integer> cooldowns) {
        for (Map.Entry<Integer, Integer> entry : cooldowns.entrySet()) {
            if (entry.getValue() > 0) {
                cooldowns.put(entry.getKey(), entry.getValue() - 1);
            }
        }
    }

    private int performAttack(PlayerMonster attacker, PlayerMonster defender, int attackerHp, int defenderHp,
            Map<Integer, Integer> cooldowns, BattleLog battleLog, boolean isMonster1) {
        // Sort skills by skill number in descending order
        List<Skill> skills = new ArrayList<>(attacker.getSkills());
        skills.sort(Comparator.comparing(Skill::getNum).reversed());

        // Select available skill
        Skill selectedSkill = null;
        for (Skill skill : skills) {
            if (cooldowns.get(skill.getNum()) == 0) {
                selectedSkill = skill;
                break;
            }
        }

        if (selectedSkill == null) {
            // All skills are on cooldown, use basic attack
            int damage = attacker.getAtk() - (defender.getDef() / 2);
            damage = Math.max(1, damage); // At least 1 damage

            defenderHp -= damage;

            // Record battle action
            BattleLog.BattleAction action = new BattleLog.BattleAction();
            action.setMonsterId(isMonster1 ? attacker.getId() : attacker.getId());
            action.setSkillNum(0); // Basic attack
            action.setDamage(damage);
            action.setTargetId(isMonster1 ? defender.getId() : defender.getId());
            action.setRemainingHp(Math.max(0, defenderHp));

            battleLog.getActions().add(action);
        } else {
            // Use selected skill
            int baseDamage = selectedSkill.getDmg();

            // Calculate stat bonus
            double statBonus = 0;
            String statType = selectedSkill.getRatio().getStat();
            double percent = selectedSkill.getRatio().getPercent() / 100.0;

            switch (statType) {
                case "atk":
                    statBonus = attacker.getAtk() * percent;
                    break;
                case "def":
                    statBonus = attacker.getDef() * percent;
                    break;
                case "hp":
                    statBonus = attackerHp * percent;
                    break;
                case "vit":
                    statBonus = attacker.getVit() * percent;
                    break;
            }

            int totalDamage = (int) (baseDamage + statBonus);
            totalDamage = Math.max(1, totalDamage - (defender.getDef() / 3)); // Defense reduction

            defenderHp -= totalDamage;

            // Set skill cooldown
            cooldowns.put(selectedSkill.getNum(), selectedSkill.getCooldown());

            // Record battle action
            BattleLog.BattleAction action = new BattleLog.BattleAction();
            action.setMonsterId(isMonster1 ? attacker.getId() : attacker.getId());
            action.setSkillNum(selectedSkill.getNum());
            action.setDamage(totalDamage);
            action.setTargetId(isMonster1 ? defender.getId() : defender.getId());
            action.setRemainingHp(Math.max(0, defenderHp));

            battleLog.getActions().add(action);
        }

        return Math.max(0, defenderHp);
    }
}