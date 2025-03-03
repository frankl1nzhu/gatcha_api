package com.gatcha.api.model;

import lombok.Data;

@Data
public class Skill {
    private String name;
    private double baseDamage;
    private StatType scalingStat;
    private double scalingRatio;
    private int cooldown;
    private int currentCooldown;
    private int level;
    private int maxLevel;

    public void levelUp() {
        level++;
        currentCooldown = 0;
    }

    public boolean isReady() {
        return currentCooldown == 0;
    }

    public void use() {
        currentCooldown = cooldown;
    }

    public void reduceCooldown() {
        currentCooldown = Math.max(0, currentCooldown - 1);
    }

    public double calculateDamage(double attackStat) {
        return baseDamage * (1 + scalingRatio * attackStat);
    }
}