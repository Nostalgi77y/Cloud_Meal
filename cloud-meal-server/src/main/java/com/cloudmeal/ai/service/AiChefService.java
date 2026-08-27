package com.cloudmeal.ai.service;

import com.cloudmeal.ai.vo.AiChefResponse;
import com.cloudmeal.product.entity.Dish;
import com.cloudmeal.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import com.cloudmeal.common.exception.BusinessException;

@Service
public class AiChefService {
    private final AiChefClient client;
    private final AiMenuMatcher matcher;
    private final ProductService productService;
    public AiChefService(AiChefClient client, AiMenuMatcher matcher, ProductService productService) {
        this.client = client; this.matcher = matcher; this.productService = productService;
    }
    public AiChefResponse analyze(Long userId, String conversationId, String ingredients, String preferences, MultipartFile image) {
        if ((ingredients == null || ingredients.isBlank()) && (image == null || image.isEmpty())) throw new BusinessException("AI_INPUT_REQUIRED", "请描述食材或上传图片");
        if (ingredients != null && ingredients.length() > 2000) throw new BusinessException("AI_INPUT_TOO_LONG", "食材描述不能超过2000字");
        if (preferences != null && preferences.length() > 1000) throw new BusinessException("AI_PREFERENCE_TOO_LONG", "偏好不能超过1000字");
        if (image != null && image.getSize() > 5 * 1024 * 1024L) throw new BusinessException("AI_IMAGE_TOO_LARGE", "食材图片不能超过5MB");
        List<Dish> menu = productService.enabledDishes(null).stream().filter(d -> d.getStock() != null && d.getStock() > 0).toList();
        String menuContext = menu.stream().limit(30).map(d -> d.getName() + "(¥" + d.getPrice() + ")").reduce((a,b) -> a + "、" + b).orElse("暂无");
        AiChefResponse ai = client.analyze(userId, conversationId, ingredients, preferences, menuContext, image);
        if (ai == null) throw new com.cloudmeal.common.exception.BusinessException("AI_EMPTY_RESPONSE", "AI私厨未返回有效结果");
        return new AiChefResponse(ai.ingredients(), ai.recipes(), ai.safetyNote(), ai.knowledgeSources(), matcher.match(ai, menu, ingredients + " " + preferences));
    }
}
