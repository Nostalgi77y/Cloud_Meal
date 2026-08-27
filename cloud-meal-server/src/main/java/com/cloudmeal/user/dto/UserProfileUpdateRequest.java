package com.cloudmeal.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank @Size(max = 80) String nickname,
        @Size(max = 500) String avatar) {
}
