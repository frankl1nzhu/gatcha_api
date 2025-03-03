package com.gatcha.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Document(collection = "monsters")
public class Monster {
    @Id
    private String id;
    private String playerId;
    private String name;
    private ElementType elementType;
    private int level;
    private double experience;
    private double experienceToNextLevel;
    private MonsterStats stats;
    private List<Skill> skills;
    private int skillPoints;
    private long createdAt;
    private long updatedAt;

    // Base stats
    private Map<StatType, Double> baseStats = new ConcurrentHashMap<>();
    // Current stats (including level bonuses)
    private Map<StatType, Double> currentStats = new ConcurrentHashMap<>();

    public void initializeStats(Map<StatType, Double> stats) {
        baseStats.putAll(stats);
        updateCurrentStats();
    }

    public void addExperience(double exp) {
        this.experience += exp;
        checkLevelUp();
        updatedAt = System.currentTimeMillis();
    }

    private void checkLevelUp() {
        while (experience >= experienceToNextLevel && level < 50) {
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        experience -= experienceToNextLevel;
        experienceToNextLevel *= 1.1;
        skillPoints++;
        updateCurrentStats();
    }

    private void updateCurrentStats() {
        // 5% increase per level
        double levelMultiplier = 1 + (level - 1) * 0.05;
        baseStats.forEach((stat, value) -> currentStats.put(stat, value * levelMultiplier));
    }

    public void upgradeSkill(int skillIndex) {
        if (skillPoints <= 0) {
            throw new IllegalStateException("No skill points available");
        }
        if (skillIndex < 0 || skillIndex >= skills.size()) {
            throw new IllegalArgumentException("Invalid skill index");
        }

        Skill skill = skills.get(skillIndex);
        skill.levelUp();
        skillPoints--;
        updatedAt = System.currentTimeMillis();
    }

    public double getStat(StatType statType) {
        return currentStats.getOrDefault(statType, 0.0);
    }

    public void reduceCooldowns() {
        skills.forEach(Skill::reduceCooldown);
    }
}