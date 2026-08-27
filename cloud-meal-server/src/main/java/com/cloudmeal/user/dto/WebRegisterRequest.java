package com.cloudmeal.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WebRegisterRequest(
        @NotBlank
        @Pattern(regexp = "^(?:[A-Za-z][A-Za-z0-9_]{2,29}|1[3-9]\\d{9})$",
                message = "账号应为3-30位字母开头的用户名，或11位手机号")
        String account,
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,32}$",
                message = "密码应为8-32位且同时包含字母和数字")
        String password,
        @Size(max = 80) String nickname) {
}
