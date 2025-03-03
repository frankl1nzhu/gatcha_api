package com.gatcha.api.dto;

import com.gatcha.api.model.BattleAction;
import com.gatcha.api.model.BattleRecord.BattleStatus;
import lombok.Data;

import java.util.List;

@Data
public class BattleResponse {
    private String battleId;
    private String monster1Id;
    private String monster2Id;
    private String winnerMonsterId;
    private String winnerPlayerId;
    private List<BattleAction> actions;
    private BattleStatus status;
    private long startTime;
    private long endTime;
}