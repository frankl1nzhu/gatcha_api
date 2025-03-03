package com.gatcha.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "players")
public class Player {
    @Id
    private String id;
    private String userId;
    private String username;
    private int level = 1;
    private double experience = 0;
    private double experienceToNextLevel = 50;
    private int maxMonsters = 10;
    private List<String> monsterIds = new ArrayList<>();
    private long createdAt = System.currentTimeMillis();
    private long updatedAt = System.currentTimeMillis();

    public void addExperience(double exp) {
        this.experience += exp;
        checkLevelUp();
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
        maxMonsters++;
    }

    public boolean canAddMonster() {
        return monsterIds.size() < maxMonsters;
    }

    public void addMonster(String monsterId) {
        if (!canAddMonster()) {
            throw new IllegalStateException("Monster list is full");
        }
        monsterIds.add(monsterId);
        updatedAt = System.currentTimeMillis();
    }

    public void removeMonster(String monsterId) {
        if (!monsterIds.remove(monsterId)) {
            throw new IllegalArgumentException("Monster not found");
        }
        updatedAt = System.currentTimeMillis();
    }
}