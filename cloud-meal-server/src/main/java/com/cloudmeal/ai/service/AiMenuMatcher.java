package com.cloudmeal.ai.service;

import com.cloudmeal.ai.vo.AiChefResponse;
import com.cloudmeal.product.entity.Dish;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class AiMenuMatcher {
    public List<AiChefResponse.DishRecommendation> match(AiChefResponse ai, List<Dish> dishes, String query) {
        if (dishes == null || dishes.isEmpty()) return List.of();
        String context = normalize(query) + normalize(ai.ingredients() == null ? "" : ai.ingredients().stream()
                .map(AiChefResponse.Ingredient::name).reduce("", (a, b) -> a + b));
        String recipeContext = normalize(ai.recipes() == null ? "" : ai.recipes().stream()
                .map(r -> r.name() + String.join("", safeList(r.ingredients())))
                .reduce("", (a, b) -> a + b));
        List<ScoredDish> scored = new ArrayList<>();
        for (Dish dish : dishes) {
            if (dish.getStock() == null || dish.getStock() <= 0 || !Integer.valueOf(1).equals(dish.getStatus())) continue;
            String name = normalize(dish.getName());
            String searchable = name + normalize(dish.getDescription());
            int score = 35;
            if (!name.isBlank() && recipeContext.contains(name)) score += 45;
            score += overlapScore(searchable, context, 30);
            score += overlapScore(searchable, recipeContext, 20);
            score = Math.min(99, score);
            String reason = score >= 80 ? "与AI菜谱高度匹配，可直接下单"
                    : score >= 60 ? "符合当前食材或口味偏好" : "店内热卖且当前有库存";
            scored.add(new ScoredDish(dish, score, reason));
        }
        return scored.stream().sorted(Comparator.comparingInt(ScoredDish::score).reversed()
                        .thenComparing(s -> s.dish().getId())).limit(3)
                .map(s -> new AiChefResponse.DishRecommendation(s.dish().getId(), s.dish().getName(),
                        s.dish().getDescription(), s.dish().getPrice(), s.dish().getImage(), s.dish().getStock(),
                        s.score(), s.reason())).toList();
    }

    private int overlapScore(String left, String right, int max) {
        if (left.isBlank() || right.isBlank()) return 0;
        Set<String> grams = bigrams(left);
        Set<String> other = bigrams(right);
        long matches = grams.stream().filter(other::contains).count();
        return grams.isEmpty() ? 0 : (int) Math.round(max * matches / (double) grams.size());
    }

    private Set<String> bigrams(String value) {
        Set<String> result = new HashSet<>();
        if (value.length() == 1) result.add(value);
        for (int i = 0; i < value.length() - 1; i++) result.add(value.substring(i, i + 2));
        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private List<String> safeList(List<String> value) { return value == null ? List.of() : value; }
    private record ScoredDish(Dish dish, int score, String reason) {}
}
