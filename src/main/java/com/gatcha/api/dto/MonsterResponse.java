package com.gatcha.api.dto;

import com.gatcha.api.model.ElementType;
import com.gatcha.api.model.MonsterStats;
import com.gatcha.api.model.Skill;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MonsterResponse {
    private String id;
    private String name;
    private ElementType elementType;
    private int level;
    private double experience;
    private double experienceToNextLevel;
    private MonsterStats stats;
    private List<Skill> skills;
    private int skillPoints;
    private long updatedAt;

    public MonsterResponse(String id, String name, ElementType elementType, int level, double experience,
            double experienceToNextLevel, MonsterStats stats, List<Skill> skills,
            int skillPoints, long updatedAt) {
        this.id = id;
        this.name = name;
        this.elementType = elementType;
        this.level = level;
        this.experience = experience;
        this.experienceToNextLevel = experienceToNextLevel;
        this.stats = stats;
        this.skills = skills;
        this.skillPoints = skillPoints;
        this.updatedAt = updatedAt;
    }
}