package com.gatcha.api.battle.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "battleLogs")
public class BattleLog {
    @Id
    private String id;
    private String monster1Id;
    private String monster2Id;
    private String winnerId;
    private Date battleDate;
    private List<BattleAction> actions = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BattleAction {
        private String monsterId;
        private int skillNum;
        private int damage;
        private String targetId;
        private int remainingHp;
    }
}