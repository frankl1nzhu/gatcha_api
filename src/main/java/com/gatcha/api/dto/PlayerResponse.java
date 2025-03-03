package com.gatcha.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlayerResponse {
    private String id;
    private String username;
    private int level;
    private double experience;
    private double experienceToNextLevel;
    private int maxMonsters;
    private List<String> monsterIds;
    private long updatedAt;
}