package com.cloudmeal.ai.vo;

import java.time.LocalDateTime;

public record AiConversationVO(String id, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {}
