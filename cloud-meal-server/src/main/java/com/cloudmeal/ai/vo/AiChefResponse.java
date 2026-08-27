package com.cloudmeal.ai.vo;

import java.math.BigDecimal;
import java.util.List;

public record AiChefResponse(List<Ingredient> ingredients, List<Recipe> recipes, String safetyNote,
                             List<KnowledgeSource> knowledgeSources, List<DishRecommendation> dishRecommendations) {
    public record Ingredient(String name, String freshness, String estimatedAmount, String observation) {}
    public record Recipe(String name, String description, Integer nutritionScore, Integer difficultyScore,
                         Integer totalScore, String cookingTime, String difficulty, List<String> ingredients,
                         List<String> steps, String recommendation, String imageUrl, String sourceUrl) {}
    public record KnowledgeSource(String fileName, String chunkId, String content, Double score) {}
    public record DishRecommendation(Long dishId, String dishName, String description, BigDecimal price,
                                     String image, Integer stock, int matchScore, String reason) {}
}
