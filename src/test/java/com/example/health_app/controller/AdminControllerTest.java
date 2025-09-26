package com.example.health_app.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.health_app.dto.DashboardDto;
import com.example.health_app.dto.UserDto;
import com.example.health_app.security.JwtAuthenticationFilter;
import com.example.health_app.security.JwtUtil;
import com.example.health_app.service.AdminService;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // SecurityFilterChainは無効化
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminService adminService;

    // セキュリティ依存解決用
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returnsListOfUsers() throws Exception {
        List<UserDto> mockUsers = List.of(
            new UserDto(1L, "taro", "taro@example.com", 20, "M", Set.of("ROLE_USER")),
            new UserDto(2L, "hanako", "hanako@example.com", 25, "F", Set.of("ROLE_USER", "ROLE_ADMIN"))
        );
        given(adminService.getAllUsers()).willReturn(mockUsers);

        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].username").value("taro"))
            .andExpect(jsonPath("$[0].email").value("taro@example.com"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].username").value("hanako"))
            .andExpect(jsonPath("$[1].roles").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDashboardData_returnsDashboardDto() throws Exception {
        DashboardDto dto = new DashboardDto(100L, 5L, 200L, 50L);
        given(adminService.getDashboardData()).willReturn(dto);

        mockMvc.perform(get("/api/admin/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalUsers").value(100))
            .andExpect(jsonPath("$.adminUsers").value(5))
            .andExpect(jsonPath("$.totalRecords").value(200))
            .andExpect(jsonPath("$.todayActiveUsers").value(50));
    }
}
