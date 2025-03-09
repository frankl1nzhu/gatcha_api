package com.gatcha.api.monster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {
    private int num;
    private int dmg;
    private Ratio ratio;
    private int cooldown;
    private int level;
    private int lvlMax;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ratio {
        private String stat;
        private double percent;
    }
}