package com.gatcha.api.summon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "summonLogs")
public class SummonLog {
    @Id
    private String id;
    private String username;
    private String templateId;
    private String monsterId;
    private Date summonDate;
    private boolean processed;

    public SummonLog(String username, String templateId) {
        this.username = username;
        this.templateId = templateId;
        this.summonDate = new Date();
        this.processed = false;
    }
}