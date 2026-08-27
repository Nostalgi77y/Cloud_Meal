package com.cloudmeal.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebLoginRequest(
        @NotBlank @Size(max = 50) String account,
        @NotBlank @Size(max = 100) String password) {
}
