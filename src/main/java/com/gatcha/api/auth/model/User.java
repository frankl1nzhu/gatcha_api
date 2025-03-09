package com.gatcha.api.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private int level;
    private int experience;
    private int maxExperience;
    private List<String> monsters = new ArrayList<>();

    public int getMaxMonsters() {
        return 10 + (level - 1);
    }

    public boolean canAddMonster() {
        return monsters.size() < getMaxMonsters();
    }

    public void addExperience(int exp) {
        this.experience += exp;
        checkLevelUp();
    }

    private void checkLevelUp() {
        if (experience >= maxExperience && level < 50) {
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        experience = 0;
        maxExperience = (int) (maxExperience * 1.1);
    }
}