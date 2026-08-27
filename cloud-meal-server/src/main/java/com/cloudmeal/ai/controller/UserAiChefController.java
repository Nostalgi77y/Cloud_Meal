package com.cloudmeal.ai.controller;

import com.cloudmeal.ai.service.AiChefClient;
import com.cloudmeal.ai.service.AiChefService;
import com.cloudmeal.ai.vo.AiChefResponse;
import com.cloudmeal.ai.vo.AiConversationVO;
import com.cloudmeal.ai.vo.AiTurnVO;
import com.cloudmeal.common.api.ApiResponse;
import com.cloudmeal.common.security.CurrentUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import com.cloudmeal.ai.dto.AiAnalyzeRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/user/ai")
public class UserAiChefController {
    private final AiChefService service;
    private final AiChefClient client;
    public UserAiChefController(AiChefService service, AiChefClient client) { this.service = service; this.client = client; }

    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public ApiResponse<AiChefResponse> analyze(@RequestParam(required = false) String conversationId,
                                               @RequestParam(required = false) String ingredients,
                                               @RequestParam(required = false) String preferences,
                                               @RequestPart(required = false) MultipartFile image) {
        return ApiResponse.success(service.analyze(CurrentUser.id(), conversationId, ingredients, preferences, image));
    }
    @PostMapping("/analyze/text")
    public ApiResponse<AiChefResponse> analyzeText(@Valid @RequestBody AiAnalyzeRequest request) {
        return ApiResponse.success(service.analyze(CurrentUser.id(), request.conversationId(), request.ingredients(), request.preferences(), null));
    }
    @PostMapping("/conversations") public ApiResponse<AiConversationVO> create(){return ApiResponse.success(client.createConversation(CurrentUser.id()));}
    @GetMapping("/conversations") public ApiResponse<List<AiConversationVO>> list(){return ApiResponse.success(client.conversations(CurrentUser.id()));}
    @GetMapping("/conversations/{id}/turns") public ApiResponse<List<AiTurnVO>> turns(@PathVariable String id){return ApiResponse.success(client.turns(CurrentUser.id(),id));}
    @DeleteMapping("/conversations/{id}/memory") public ApiResponse<Void> clear(@PathVariable String id){client.clear(CurrentUser.id(),id);return ApiResponse.success();}
    @DeleteMapping("/conversations/{id}") public ApiResponse<Void> delete(@PathVariable String id){client.delete(CurrentUser.id(),id);return ApiResponse.success();}
}
