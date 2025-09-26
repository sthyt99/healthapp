package com.example.health_app.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.example.health_app.security.JwtAuthenticationFilter;
import com.example.health_app.security.JwtUtil;
import com.example.health_app.service.HealthRecordService;

@WebMvcTest(controllers = AdminRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthRecordService recordService;
    
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean JwtUtil jwtUtil;

    // ---- helpers ----
    private HealthRecord record(long id, LocalDate date, double weight) {
        HealthRecord r = new HealthRecord();
        r.setId(id);
        r.setRecordDate(date);
        r.setWeight(weight);
        return r;
    }

    // 任意ユーザーの記録作成
    @Test
    void createRecordForUser_returns200_andDto() throws Exception {
        var saved = record(10L, LocalDate.of(2025, 9, 26), 65.2);
        given(recordService.createRecord(eq(1L), any(HealthRecordRequestDto.class))).willReturn(saved);

        mockMvc.perform(post("/api/admin/records/{userId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"recordDate":"2025-09-26","weight":65.2}
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.weight").value(65.2));
    }

    // 任意ユーザーの記録取得
    @Test
    void getRecordsByUser_returns200_andList() throws Exception {
        var rec1 = record(101L, LocalDate.of(2025, 9, 25), 60.0);
        var rec2 = record(102L, LocalDate.of(2025, 9, 26), 61.0);
        given(recordService.getRecordsByUserId(1L)).willReturn(List.of(rec1, rec2));

        mockMvc.perform(get("/api/admin/records/{userId}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(101))
            .andExpect(jsonPath("$[0].weight").value(60.0))
            .andExpect(jsonPath("$[1].id").value(102))
            .andExpect(jsonPath("$[1].weight").value(61.0));
    }

    // 任意ユーザーの記録削除
    @Test
    void deleteRecord_returns200_andMessage() throws Exception {
        doNothing().when(recordService).deleteRecordForAdmin(999L);

        mockMvc.perform(delete("/api/admin/records/{recordId}", 999L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(AppMessage.RECORD_DELETE));
    }
}
