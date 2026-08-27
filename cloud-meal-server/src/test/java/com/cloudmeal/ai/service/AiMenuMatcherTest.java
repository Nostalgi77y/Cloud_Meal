package com.cloudmeal.ai.service;

import com.cloudmeal.ai.vo.AiChefResponse;
import com.cloudmeal.product.entity.Dish;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiMenuMatcherTest {
    private final AiMenuMatcher matcher = new AiMenuMatcher();

    @Test
    void onlyReturnsAvailableRealDishesAndRanksRelevantDishFirst() {
        Dish chicken = dish(1L, "云膳香煎鸡排", "高蛋白鸡胸肉", 10, 1);
        Dish tea = dish(2L, "冰柠檬茶", "清爽饮品", 20, 1);
        Dish soldOut = dish(3L, "鸡胸肉沙拉", "低脂", 0, 1);
        AiChefResponse ai = new AiChefResponse(List.of(new AiChefResponse.Ingredient("鸡胸肉", "新鲜", "200克", "")),
                List.of(new AiChefResponse.Recipe("香煎鸡胸肉", "", 90, 90, 90, "20分钟", "简单", List.of("鸡胸肉"), List.of("煎熟"), "", "", "")),
                "", List.of(), null);

        var result = matcher.match(ai, List.of(tea, soldOut, chicken), "低脂高蛋白");

        assertEquals(2, result.size());
        assertEquals(chicken.getId(), result.get(0).dishId());
        assertTrue(result.stream().noneMatch(item -> item.dishId().equals(soldOut.getId())));
    }

    private Dish dish(Long id, String name, String description, int stock, int status) {
        Dish dish = new Dish(); dish.setId(id); dish.setName(name); dish.setDescription(description);
        dish.setStock(stock); dish.setStatus(status); dish.setPrice(new BigDecimal("18.00")); return dish;
    }
}
