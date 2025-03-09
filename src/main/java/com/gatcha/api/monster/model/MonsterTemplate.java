package com.gatcha.api.monster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "monsterTemplates")
public class MonsterTemplate {
    @Id
    private Integer id;
    private String element;
    private int hp;
    private int atk;
    private int def;
    private int vit;
    private List<Skill> skills;
    private double lootRate;
}