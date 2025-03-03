package com.gatcha.api.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ExperienceRequest {
    @Positive(message = "Experience must be positive")
    private double experience;
}