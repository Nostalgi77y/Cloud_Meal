package com.cloudmeal.user.controller;

import com.cloudmeal.common.api.ApiResponse;
import com.cloudmeal.common.security.CurrentUser;
import com.cloudmeal.user.dto.UserProfileUpdateRequest;
import com.cloudmeal.user.service.WebUserAccountService;
import com.cloudmeal.user.vo.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/profile")
public class UserProfileController {
    private final WebUserAccountService service;
    public UserProfileController(WebUserAccountService service) { this.service = service; }

    @GetMapping
    public ApiResponse<UserProfileVO> profile() { return ApiResponse.success(service.profile(CurrentUser.id())); }

    @PutMapping
    public ApiResponse<UserProfileVO> update(@Valid @RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.success(service.updateProfile(CurrentUser.id(), request));
    }
}
