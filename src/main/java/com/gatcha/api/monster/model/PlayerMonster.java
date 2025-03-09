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
@Document(collection = "playerMonsters")
public class PlayerMonster {
    @Id
    private String id;
    private String username;
    private String templateId;
    private String element;
    private int level;
    private int experience;
    private int hp;
    private int atk;
    private int def;
    private int vit;
    private List<Skill> skills;
    private int skillPoints;

    public void addExperience(int exp) {
        this.experience += exp;
        checkLevelUp();
    }

    private void checkLevelUp() {
        if (experience >= 100) {
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        experience = 0;
        // Increase base attributes
        hp += 50;
        atk += 10;
        def += 10;
        vit += 5;
        // Increase skill points
        skillPoints++;
    }

    public void upgradeSkill(int skillNum) {
        if (skillPoints <= 0) {
            throw new IllegalStateException("No skill points available");
        }

        for (Skill skill : skills) {
            if (skill.getNum() == skillNum && skill.getLevel() < skill.getLvlMax()) {
                // Upgrade skill level
                skill.setLevel(skill.getLevel() + 1);

                // Increase skill damage (10% base damage per level)
                int baseDmg = skill.getDmg();
                int newDmg = (int) (baseDmg * (1 + 0.1 * skill.getLevel()));
                skill.setDmg(newDmg);

                // Increase skill ratio (5% base ratio per level)
                Skill.Ratio ratio = skill.getRatio();
                if (ratio != null) {
                    double basePercent = ratio.getPercent();
                    double newPercent = basePercent * (1 + 0.05 * skill.getLevel());
                    ratio.setPercent(newPercent);
                }

                // Reduce skill cooldown (1 point reduction every 2 levels, minimum half of
                // original)
                if (skill.getLevel() % 2 == 0 && skill.getCooldown() > 0) {
                    int originalCooldown = skill.getCooldown();
                    int minCooldown = Math.max(1, originalCooldown / 2);
                    int newCooldown = Math.max(minCooldown, originalCooldown - (skill.getLevel() / 2));
                    skill.setCooldown(newCooldown);
                }

                // Consume skill point
                skillPoints--;
                return;
            }
        }

        throw new IllegalArgumentException("Skill not found or already at max level");
    }
}