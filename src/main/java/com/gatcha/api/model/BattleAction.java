package com.gatcha.api.model;

import lombok.Data;

@Data
public class BattleAction {
    private String monsterId;
    private String monsterName;
    private int skillIndex;
    private String skillName;
    private double damage;
    private long timestamp = System.currentTimeMillis();
}