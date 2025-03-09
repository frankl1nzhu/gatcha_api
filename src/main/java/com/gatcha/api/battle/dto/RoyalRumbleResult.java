package com.gatcha.api.battle.dto;

import com.gatcha.api.battle.model.BattleLog;
import com.gatcha.api.monster.model.PlayerMonster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Royal Rumble Result DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoyalRumbleResult {
    /**
     * Royal Rumble ID
     */
    private String id;

    /**
     * List of monster IDs participating in the Royal Rumble
     */
    private List<String> participantIds;

    /**
     * Winner monster
     */
    private PlayerMonster winner;

    /**
     * Royal Rumble date
     */
    private Date rumbleDate;

    /**
     * Royal Rumble rounds
     */
    private List<RumbleRound> rounds;

    /**
     * Experience gained
     */
    private int experienceGained;

    /**
     * Royal Rumble round
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RumbleRound {
        /**
         * Round number
         */
        private int roundNumber;

        /**
         * Battle actions in the round
         */
        private List<BattleLog.BattleAction> actions;

        /**
         * List of remaining monster IDs after the round
         */
        private List<String> remainingMonsterIds;
    }
}