package com.cloudmeal.ai.service;

import com.cloudmeal.ai.config.AiChefProperties;
import com.cloudmeal.ai.vo.AiChefResponse;
import com.cloudmeal.ai.vo.AiConversationVO;
import com.cloudmeal.ai.vo.AiTurnVO;
import com.cloudmeal.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

@Component
public class AiChefClient {
    private final RestClient client;
    private final AiChefProperties properties;
    private final ObjectMapper objectMapper;

    public AiChefClient(AiChefProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());
        this.client = RestClient.builder().baseUrl(properties.getBaseUrl()).requestFactory(requestFactory).build();
    }

    public AiChefResponse analyze(Long userId, String conversationId, String ingredients, String preferences,
                                  String menuContext, MultipartFile image) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        add(body, "conversationId", conversationId);
        add(body, "ingredients", ingredients);
        add(body, "preferences", appendMenu(preferences, menuContext));
        if (image != null && !image.isEmpty()) {
            try {
                HttpHeaders partHeaders = new HttpHeaders();
                partHeaders.setContentType(MediaType.parseMediaType(image.getContentType() == null ? "image/jpeg" : image.getContentType()));
                body.add("image", new HttpEntity<>(new NamedByteArrayResource(image.getBytes(), image.getOriginalFilename()), partHeaders));
            } catch (IOException e) {
                throw new BusinessException("AI_IMAGE_READ_FAILED", "食材图片读取失败");
            }
        }
        try {
            return client.post().uri("/api/chef/analyze").headers(h -> headers(h, userId))
                    .contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(AiChefResponse.class);
        } catch (ResourceAccessException e) {
            throw new BusinessException("AI_SERVICE_TIMEOUT", "AI私厨响应超时，请稍后重试");
        } catch (RestClientResponseException e) {
            throw upstream(e);
        } catch (RestClientException e) {
            if (hasCause(e, SocketTimeoutException.class)) {
                throw new BusinessException("AI_SERVICE_TIMEOUT", "AI私厨响应超时，请稍后重试");
            }
            throw new BusinessException("AI_SERVICE_ERROR", "AI私厨响应格式异常，请稍后重试");
        }
    }

    public AiConversationVO createConversation(Long userId) { return exchange(userId, () -> client.post().uri("/api/conversations").headers(h -> headers(h, userId)).retrieve().body(AiConversationVO.class)); }
    public List<AiConversationVO> conversations(Long userId) { return exchange(userId, () -> client.get().uri("/api/conversations").headers(h -> headers(h, userId)).retrieve().body(new ParameterizedTypeReference<>() {})); }
    public List<AiTurnVO> turns(Long userId, String id) { return exchange(userId, () -> client.get().uri("/api/conversations/{id}/turns", id).headers(h -> headers(h, userId)).retrieve().body(new ParameterizedTypeReference<>() {})); }
    public void clear(Long userId, String id) { exchange(userId, () -> { client.delete().uri("/api/conversations/{id}/memory", id).headers(h -> headers(h, userId)).retrieve().toBodilessEntity(); return null; }); }
    public void delete(Long userId, String id) { exchange(userId, () -> { client.delete().uri("/api/conversations/{id}", id).headers(h -> headers(h, userId)).retrieve().toBodilessEntity(); return null; }); }

    private <T> T exchange(Long userId, RequestCall<T> call) {
        try { return call.call(); }
        catch (ResourceAccessException e) { throw new BusinessException("AI_SERVICE_UNAVAILABLE", "AI私厨服务暂不可用"); }
        catch (RestClientResponseException e) { throw upstream(e); }
    }

    private void headers(org.springframework.http.HttpHeaders headers, Long userId) {
        headers.set("X-Cloud-User-Id", userId.toString());
        if (properties.getServiceToken() != null && !properties.getServiceToken().isBlank()) headers.set("X-Service-Token", properties.getServiceToken());
    }

    private BusinessException upstream(RestClientResponseException e) {
        try {
            JsonNode node = objectMapper.readTree(e.getResponseBodyAsString());
            String message = node.path("message").asText("AI私厨处理失败");
            String code = e.getStatusCode().value() == 503 && message.contains("超时")
                    ? "AI_SERVICE_TIMEOUT"
                    : e.getStatusCode().value() == 503 ? "AI_SERVICE_UNAVAILABLE" : "AI_SERVICE_ERROR";
            return new BusinessException(code, message);
        } catch (Exception ignored) { return new BusinessException("AI_SERVICE_ERROR", "AI私厨处理失败"); }
    }

    private String appendMenu(String preferences, String menuContext) {
        String base = preferences == null ? "" : preferences.trim();
        return base + "\n如推荐直接购买，请优先参考云膳当前在售菜单：" + menuContext;
    }
    private void add(MultiValueMap<String,Object> body,String key,String value){if(value!=null&&!value.isBlank())body.add(key,value);}
    private boolean hasCause(Throwable error, Class<? extends Throwable> type) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            if (type.isInstance(current)) return true;
        }
        return false;
    }
    @FunctionalInterface private interface RequestCall<T> { T call(); }
    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;
        NamedByteArrayResource(byte[] bytes, String filename) { super(bytes); this.filename = filename == null ? "ingredients.jpg" : filename; }
        @Override public String getFilename() { return filename; }
    }
}
