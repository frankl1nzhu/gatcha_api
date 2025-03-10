package com.gatcha.api.battle.service;

import com.gatcha.api.battle.model.BattleLog;

import java.util.List;

public interface BattleService {
    BattleLog battle(String monster1Id, String monster2Id, String username);

    BattleLog getBattleById(String battleId);

    List<BattleLog> getBattlesByMonsterId(String monsterId);

    int getExperienceGained(String battleId);

    List<BattleLog> getAllBattles();
}