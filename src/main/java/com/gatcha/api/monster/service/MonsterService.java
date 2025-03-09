package com.gatcha.api.monster.service;

import com.gatcha.api.monster.model.PlayerMonster;

import java.util.List;

public interface MonsterService {
    List<PlayerMonster> getMonstersByUsername(String username);

    PlayerMonster getMonsterById(String id, String username);

    PlayerMonster addExperience(String id, String username, int experience);

    PlayerMonster upgradeSkill(String id, String username, int skillNum);

    PlayerMonster createMonsterFromTemplate(Integer templateId, String username);
}