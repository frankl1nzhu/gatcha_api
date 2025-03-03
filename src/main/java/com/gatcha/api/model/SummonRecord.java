package com.gatcha.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "summons")
public class SummonRecord {
    @Id
    private String id;
    private String playerId;
    private String templateId;
    private String monsterId;
    private String monsterName;
    private ElementType elementType;
    private boolean isProcessed;
    private long createdAt = System.currentTimeMillis();
    private long processedAt;
}