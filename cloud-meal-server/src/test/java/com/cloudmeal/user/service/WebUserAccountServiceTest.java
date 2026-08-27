package com.cloudmeal.user.service;

import com.cloudmeal.auth.security.JwtService;
import com.cloudmeal.auth.vo.LoginResponse;
import com.cloudmeal.common.exception.BusinessException;
import com.cloudmeal.user.dto.WebLoginRequest;
import com.cloudmeal.user.dto.WebRegisterRequest;
import com.cloudmeal.user.entity.User;
import com.cloudmeal.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebUserAccountServiceTest {
    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @Test
    void registerCreatesAUniquePasswordAccountAndUsesDatabaseUserId() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("Cloud1234")).thenReturn("bcrypt-hash");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(88L);
            return 1;
        });
        when(jwtService.create(88L, "cloud_user", "USER")).thenReturn("jwt-token");
        WebUserAccountService service = new WebUserAccountService(userMapper, passwordEncoder, jwtService);

        LoginResponse response = service.register(new WebRegisterRequest("cloud_user", "Cloud1234", "云膳会员"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("cloud_user", captor.getValue().getUsername());
        assertNull(captor.getValue().getPhone());
        assertEquals("bcrypt-hash", captor.getValue().getPassword());
        assertEquals(88L, response.userId());
        assertEquals("jwt-token", response.token());
    }

    @Test
    void loginRejectsAnIncorrectPassword() {
        User existing = new User();
        existing.setId(9L);
        existing.setUsername("member_9");
        existing.setPassword("bcrypt-hash");
        existing.setStatus(1);
        when(userMapper.selectOne(any())).thenReturn(existing);
        when(passwordEncoder.matches("wrong-pass", "bcrypt-hash")).thenReturn(false);
        WebUserAccountService service = new WebUserAccountService(userMapper, passwordEncoder, jwtService);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.login(new WebLoginRequest("member_9", "wrong-pass")));
        assertEquals("AUTH_FAILED", error.getCode());
        verify(jwtService, never()).create(anyLong(), anyString(), anyString());
    }
}
