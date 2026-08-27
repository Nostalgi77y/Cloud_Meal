package com.cloudmeal.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiAnalyzeRequest(String conversationId,
                               @NotBlank @Size(max = 2000) String ingredients,
                               @Size(max = 1000) String preferences) {}
