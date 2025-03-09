package com.gatcha.api.battle.dto;

import com.gatcha.api.battle.model.BattleLog;
import com.gatcha.api.monster.model.PlayerMonster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleResponse {
    private BattleLog battleLog;
    private PlayerMonster winner;
    private int experienceGained;
}