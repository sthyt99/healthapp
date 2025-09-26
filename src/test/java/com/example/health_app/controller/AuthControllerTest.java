package com.example.health_app.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.health_app.constant.AppMessage;
import com.example.health_app.entity.User;
import com.example.health_app.security.JwtAuthenticationFilter;
import com.example.health_app.security.JwtUtil;
import com.example.health_app.service.UserService;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    // ---- mocks ----
    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean UserService userService;
    @MockitoBean JwtUtil jwtUtil;

    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;

    // ---- helper ----
    private static User userEntity(long id, String username, String email) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setRoles(Collections.emptySet()); // ここは空でOK（レスポンスの roles は []）
        u.setRoleVersion(0L);
        return u;
    }

    @Test
    void login_success_returnsTokenDto() throws Exception {
        // 認証成功スタブ（認証自体は値を返せばOK）
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .willAnswer(inv -> inv.getArgument(0));

        // ユーザー取得
        var user = userEntity(1L, "taro", "taro@example.com");
        given(userService.findByUsernameOrThrow("taro")).willReturn(user);

        // トークン生成
        given(jwtUtil.generateToken(eq("taro"), eq(user.getRoles()), eq(user.getRoleVersion())))
            .willReturn("jwt-token-123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"taro","password":"Passw0rd!"}
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token-123"))
            .andExpect(jsonPath("$.username").value("taro"))
            .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void login_failure_returns401WithMessage() throws Exception {
        // Spring Security は AuthenticationException のサブクラスなら何でもOK
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .willThrow(new AuthenticationServiceException("bad creds"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"taro","password":"wrong"}
                """))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(AppMessage.AUTH_FAILED));
    }
}
