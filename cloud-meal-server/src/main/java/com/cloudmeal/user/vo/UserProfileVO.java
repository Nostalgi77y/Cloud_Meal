package com.cloudmeal.user.vo;

import java.time.LocalDateTime;

public record UserProfileVO(Long userId, String account, String username, String phone,
                            String nickname, String avatar, LocalDateTime createdTime) {
}
