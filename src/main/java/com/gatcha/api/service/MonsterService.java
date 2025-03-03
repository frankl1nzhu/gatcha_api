package com.gatcha.api.service;

import com.gatcha.api.dto.MonsterResponse;
import com.gatcha.api.model.Monster;
import com.gatcha.api.model.Player;
import com.gatcha.api.repository.MonsterRepository;
import com.gatcha.api.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonsterService {
    private final MonsterRepository monsterRepository;
    private final PlayerRepository playerRepository;

    public List<MonsterResponse> getPlayerMonsters(String username) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        return monsterRepository.findByPlayerId(player.getId()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public MonsterResponse getMonster(String username, String monsterId) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() -> new IllegalArgumentException("Monster not found"));

        if (!monster.getPlayerId().equals(player.getId())) {
            throw new IllegalArgumentException("Access denied to this monster");
        }

        return convertToResponse(monster);
    }

    @Transactional
    public MonsterResponse addExperience(String username, String monsterId, double experience) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() -> new IllegalArgumentException("Monster not found"));

        if (!monster.getPlayerId().equals(player.getId())) {
            throw new IllegalArgumentException("Access denied to this monster");
        }

        // Add experience
        monster.setExperience(monster.getExperience() + experience);

        // Check for level up
        while (monster.getExperience() >= monster.getExperienceToNextLevel()) {
            // Level up
            monster.setLevel(monster.getLevel() + 1);
            monster.setExperience(monster.getExperience() - monster.getExperienceToNextLevel());

            // Calculate next level experience requirement
            monster.setExperienceToNextLevel(calculateExperienceToNextLevel(monster.getLevel()));

            // Increase stats
            updateStatsOnLevelUp(monster);

            // Grant skill point
            monster.setSkillPoints(monster.getSkillPoints() + 1);
        }

        monster.setUpdatedAt(System.currentTimeMillis());
        monsterRepository.save(monster);

        return convertToResponse(monster);
    }

    @Transactional
    public MonsterResponse upgradeSkill(String username, String monsterId, int skillIndex) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() -> new IllegalArgumentException("Monster not found"));

        if (!monster.getPlayerId().equals(player.getId())) {
            throw new IllegalArgumentException("Access denied to this monster");
        }

        if (monster.getSkillPoints() <= 0) {
            throw new IllegalArgumentException("Not enough skill points");
        }

        if (skillIndex < 0 || skillIndex >= monster.getSkills().size()) {
            throw new IllegalArgumentException("Invalid skill index");
        }

        var skill = monster.getSkills().get(skillIndex);
        if (skill.getLevel() >= skill.getMaxLevel()) {
            throw new IllegalArgumentException("Skill already at max level");
        }

        // Upgrade skill
        skill.setLevel(skill.getLevel() + 1);
        monster.setSkillPoints(monster.getSkillPoints() - 1);
        monster.setUpdatedAt(System.currentTimeMillis());
        monsterRepository.save(monster);

        return convertToResponse(monster);
    }

    private double calculateExperienceToNextLevel(int level) {
        // Base experience is 50, increases by 20% per level
        return 50 * Math.pow(1.2, level - 1);
    }

    private void updateStatsOnLevelUp(Monster monster) {
        // Increase base stats by 5%
        monster.getStats().setHp(monster.getStats().getHp() * 1.05);
        monster.getStats().setAtk(monster.getStats().getAtk() * 1.05);
        monster.getStats().setDef(monster.getStats().getDef() * 1.05);
        monster.getStats().setSpd(monster.getStats().getSpd() * 1.05);
    }

    private MonsterResponse convertToResponse(Monster monster) {
        return new MonsterResponse(
                monster.getId(),
                monster.getName(),
                monster.getElementType(),
                monster.getLevel(),
                monster.getExperience(),
                monster.getExperienceToNextLevel(),
                monster.getStats(),
                monster.getSkills(),
                monster.getSkillPoints(),
                monster.getUpdatedAt());
    }
}