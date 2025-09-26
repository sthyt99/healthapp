package com.example.health_app.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.health_app.constant.AppMessage;
import com.example.health_app.dto.HealthRecordRequestDto;
import com.example.health_app.entity.HealthRecord;
import com.example.health_app.entity.User;
import com.example.health_app.security.JwtAuthenticationFilter;
import com.example.health_app.security.JwtUtil;
import com.example.health_app.service.HealthRecordService;
import com.example.health_app.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(controllers = UserRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthRecordService recordService;

    @MockitoBean
    private UserService userService;
    
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean JwtUtil jwtUtil;

    private final Principal principal = () -> "testuser";

    @Test
    void createMyRecord_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        HealthRecord record = new HealthRecord();
        record.setId(10L);
        record.setRecordDate(LocalDate.of(2025, 9, 26));
        record.setWeight(65.2);

        given(userService.findByUsernameOrThrow("testuser")).willReturn(user);
        given(recordService.createRecord(eq(1L), any(HealthRecordRequestDto.class))).willReturn(record);

        mockMvc.perform(post("/api/records/me")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordDate\":\"2025-09-26\",\"weight\":65.2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.weight").value(65.2));
    }

    @Test
    void getMyRecords_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        HealthRecord rec1 = new HealthRecord();
        rec1.setId(101L);
        rec1.setRecordDate(LocalDate.of(2025, 9, 26));
        rec1.setWeight(60.0);

        given(userService.findByUsernameOrThrow("testuser")).willReturn(user);
        given(recordService.getRecordsByUserId(1L)).willReturn(List.of(rec1));

        mockMvc.perform(get("/api/records/me").principal(principal))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(101))
            .andExpect(jsonPath("$[0].weight").value(60.0));
    }

    @Test
    void deleteMyRecord_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        given(userService.findByUsernameOrThrow("testuser")).willReturn(user);

        mockMvc.perform(delete("/api/records/{id}", 999L).principal(principal))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(AppMessage.RECORD_DELETE));
    }

    @Test
    void deleteMyRecord_notFound() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        given(userService.findByUsernameOrThrow("testuser")).willReturn(user);
        doThrow(new EntityNotFoundException()).when(recordService).deleteRecordAs(eq(999L), eq(user));

        mockMvc.perform(delete("/api/records/{id}", 999L).principal(principal))
            .andExpect(status().isNotFound());
    }
}
