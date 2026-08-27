package com.cloudmeal.ai.vo;

import java.time.LocalDateTime;

public record AiTurnVO(Long id, String conversationId, String userText, String responseJson, LocalDateTime createdAt) {}
