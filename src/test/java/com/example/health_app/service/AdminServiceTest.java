package com.example.health_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.health_app.dto.DashboardDto;
import com.example.health_app.dto.UserDto;
import com.example.health_app.entity.Role;
import com.example.health_app.entity.User;
import com.example.health_app.repository.HealthRecordRepository;
import com.example.health_app.repository.UserRepository;

/**
 * Mockの単体テスト用
 * ・ADMIN数を集計
 * ・本日のアクティブユーザーを算出
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserRepository userRepository;
    @Mock HealthRecordRepository healthRecordRepository;

    @InjectMocks AdminService adminService;

    @Test
    void getAllUsers_mapsToDto() {
        // given
        User u1 = new User();
        u1.setId(1L); u1.setUsername("alice"); u1.setEmail("alice@example.com");
        u1.setRoles(Set.of(Role.ROLE_USER));

        User u2 = new User();
        u2.setId(2L); u2.setUsername("admin"); u2.setEmail("admin@example.com");
        u2.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        // when
        List<UserDto> dtos = adminService.getAllUsers();

        // then
        assertEquals(2, dtos.size());
        var d1 = dtos.get(0);
        var d2 = dtos.get(1);

        assertEquals(1L, d1.getId());
        assertEquals("alice", d1.getUsername());
        assertTrue(d1.getRoles().contains("ROLE_USER"));

        assertEquals(2L, d2.getId());
        assertEquals("admin", d2.getUsername());
        assertTrue(d2.getRoles().contains("ROLE_ADMIN"));
        assertTrue(d2.getRoles().contains("ROLE_USER"));

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(healthRecordRepository);
    }

    @Test
    void getDashboardData_aggregatesCounts() {
        // totalUsers / totalRecords
        when(userRepository.count()).thenReturn(10L);
        when(healthRecordRepository.count()).thenReturn(123L);

        when(userRepository.countByRole(Role.ROLE_ADMIN)).thenReturn(1L);
        when(healthRecordRepository.countDistinctUsersByRecordDate(any(LocalDate.class))).thenReturn(2L);

        DashboardDto dto = adminService.getDashboardData();

        assertEquals(10L, dto.getTotalUsers());
        assertEquals(1L, dto.getAdminUsers());
        assertEquals(123L, dto.getTotalRecords());
        assertEquals(2L, dto.getTodayActiveUsers());

        verify(userRepository).count();
        verify(healthRecordRepository).count();
        verify(userRepository).countByRole(Role.ROLE_ADMIN);
        verify(healthRecordRepository).countDistinctUsersByRecordDate(any(LocalDate.class));
        verifyNoMoreInteractions(userRepository, healthRecordRepository);
    }
}