package com.cloudmeal.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudmeal.auth.security.JwtService;
import com.cloudmeal.auth.vo.LoginResponse;
import com.cloudmeal.common.exception.BusinessException;
import com.cloudmeal.user.dto.UserProfileUpdateRequest;
import com.cloudmeal.user.dto.WebLoginRequest;
import com.cloudmeal.user.dto.WebRegisterRequest;
import com.cloudmeal.user.entity.User;
import com.cloudmeal.user.mapper.UserMapper;
import com.cloudmeal.user.vo.UserProfileVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class WebUserAccountService {
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public WebUserAccountService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse register(WebRegisterRequest request) {
        String account = request.account().trim();
        if (findByAccount(account) != null) throw new BusinessException("ACCOUNT_EXISTS", "用户名或手机号已注册");
        boolean phoneAccount = account.matches(PHONE_PATTERN);
        User user = new User();
        if (phoneAccount) user.setPhone(account); else user.setUsername(account);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(StringUtils.hasText(request.nickname()) ? request.nickname().trim()
                : phoneAccount ? "云膳用户" + account.substring(7) : account);
        user.setStatus(1);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException duplicate) {
            throw new BusinessException("ACCOUNT_EXISTS", "用户名或手机号已注册");
        }
        return session(user, account);
    }

    public LoginResponse login(WebLoginRequest request) {
        String account = request.account().trim();
        User user = findByAccount(account);
        if (user == null || !StringUtils.hasText(user.getPassword())
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("AUTH_FAILED", "账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException("USER_DISABLED", "账号已被禁用");
        return session(user, account);
    }

    public UserProfileVO profile(Long userId) {
        User user = requireUser(userId);
        return toProfile(user);
    }

    @Transactional
    public UserProfileVO updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = requireUser(userId);
        user.setNickname(request.nickname().trim());
        user.setAvatar(StringUtils.hasText(request.avatar()) ? request.avatar().trim() : null);
        userMapper.updateById(user);
        return toProfile(user);
    }

    private User findByAccount(String account) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .and(query -> query.eq(User::getUsername, account).or().eq(User::getPhone, account)));
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        return user;
    }

    private LoginResponse session(User user, String account) {
        String token = jwtService.create(user.getId(), account, "USER");
        return new LoginResponse(token, user.getId(), user.getNickname(), "USER");
    }

    private UserProfileVO toProfile(User user) {
        String account = StringUtils.hasText(user.getUsername()) ? user.getUsername()
                : StringUtils.hasText(user.getPhone()) ? user.getPhone() : "微信用户";
        return new UserProfileVO(user.getId(), account, user.getUsername(), user.getPhone(),
                user.getNickname(), user.getAvatar(), user.getCreatedTime());
    }
}
