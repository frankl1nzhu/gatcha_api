package com.gatcha.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpgradeSkillRequest {
    @Min(value = 0, message = "Skill index must be non-negative")
    private int skillIndex;
}