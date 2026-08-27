package com.cloudmeal.user.controller;

import com.cloudmeal.auth.security.JwtService;
import com.cloudmeal.auth.vo.LoginResponse;
import com.cloudmeal.common.api.ApiResponse;
import com.cloudmeal.user.dto.WechatLoginRequest;
import com.cloudmeal.user.dto.WebLoginRequest;
import com.cloudmeal.user.dto.WebRegisterRequest;
import com.cloudmeal.user.service.WebUserAccountService;
import com.cloudmeal.user.service.WechatLoginService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/auth")
public class UserAuthController {
    private final JwtService jwtService;
    private final WechatLoginService wechatLoginService;
    private final WebUserAccountService webUserAccountService;
    public UserAuthController(JwtService jwtService, WechatLoginService wechatLoginService,
                              WebUserAccountService webUserAccountService) {
        this.jwtService = jwtService;
        this.wechatLoginService = wechatLoginService;
        this.webUserAccountService = webUserAccountService;
    }

    @PostMapping("/wechat-login")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(wechatLoginService.login(request.code()));
    }

    @PostMapping("/demo-login")
    public ApiResponse<LoginResponse> demoLogin() {
        String token = jwtService.create(1L, "demo-openid", "USER");
        return ApiResponse.success(new LoginResponse(token, 1L, "演示用户", "USER"));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody WebRegisterRequest request) {
        return ApiResponse.success(webUserAccountService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody WebLoginRequest request) {
        return ApiResponse.success(webUserAccountService.login(request));
    }
}
