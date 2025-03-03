package com.gatcha.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BattleRequest {
    @NotBlank(message = "Opponent monster ID is required")
    private String opponentMonsterId;

    @NotBlank(message = "Player monster ID is required")
    private String playerMonsterId;
}