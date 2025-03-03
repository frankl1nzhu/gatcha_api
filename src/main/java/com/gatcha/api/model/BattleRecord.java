package com.gatcha.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "battles")
public class BattleRecord {
    @Id
    private String id;
    private String player1Id;
    private String player2Id;
    private String monster1Id;
    private String monster2Id;
    private String winnerMonsterId;
    private String winnerPlayerId;
    private List<BattleAction> actions = new ArrayList<>();
    private BattleStatus status = BattleStatus.IN_PROGRESS;
    private long startTime = System.currentTimeMillis();
    private long endTime;

    public enum BattleStatus {
        IN_PROGRESS,
        COMPLETED,
        ERROR
    }

    public void addAction(BattleAction action) {
        actions.add(action);
    }

    public void complete(String winnerMonsterId, String winnerPlayerId) {
        this.winnerMonsterId = winnerMonsterId;
        this.winnerPlayerId = winnerPlayerId;
        this.status = BattleStatus.COMPLETED;
        this.endTime = System.currentTimeMillis();
    }

    public void error() {
        this.status = BattleStatus.ERROR;
        this.endTime = System.currentTimeMillis();
    }
}