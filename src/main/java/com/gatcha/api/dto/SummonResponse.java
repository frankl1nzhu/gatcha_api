package com.gatcha.api.dto;

import com.gatcha.api.model.ElementType;
import lombok.Data;

@Data
public class SummonResponse {
    private String summonId;
    private String monsterId;
    private String monsterName;
    private ElementType elementType;
    private long summonedAt;
}