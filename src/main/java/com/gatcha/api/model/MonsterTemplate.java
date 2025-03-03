package com.gatcha.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Document(collection = "monster_templates")
public class MonsterTemplate {
    @Id
    private String id;
    private String name;
    private ElementType elementType;
    private double summonRate; // Summon probability

    // Base attributes
    private Map<StatType, Double> baseStats = new ConcurrentHashMap<>();

    // Skill templates
    private List<Skill> skillTemplates = new ArrayList<>();

    public Monster createMonster(String playerId) {
        Monster monster = new Monster();
        monster.setPlayerId(playerId);
        monster.setName(name);
        monster.setElementType(elementType);
        monster.initializeStats(baseStats);

        // Deep copy skill list
        List<Skill> newSkills = new ArrayList<>();
        for (Skill template : skillTemplates) {
            Skill skill = new Skill();
            skill.setName(template.getName());
            skill.setBaseDamage(template.getBaseDamage());
            skill.setScalingStat(template.getScalingStat());
            skill.setScalingRatio(template.getScalingRatio());
            skill.setCooldown(template.getCooldown());
            skill.setMaxLevel(template.getMaxLevel());
            newSkills.add(skill);
        }
        monster.setSkills(newSkills);

        return monster;
    }
}