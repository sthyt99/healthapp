package com.example.health_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.health_app.dto.HealthRecordRequestDto;
import com.example.health_app.entity.HealthRecord;
import com.example.health_app.entity.Role;
import com.example.health_app.entity.User;
import com.example.health_app.repository.HealthRecordRepository;
import com.example.health_app.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Mockの単体テスト用
 * ・記録作成（成功・ユーザー無し）
 * ・記録取得
 * ・削除（成功・権限無し）
 */
@ExtendWith(MockitoExtension.class)
class HealthRecordServiceTest {

	@Mock
	HealthRecordRepository recordRepository;
	@Mock
	UserRepository userRepository;

	@InjectMocks
	HealthRecordService service;

	@Test
	void createRecord_success() {
		User user = new User();
		user.setId(10L);
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));
		when(recordRepository.save(any(HealthRecord.class)))
				.thenAnswer(inv -> inv.getArgument(0));

		HealthRecordRequestDto dto = new HealthRecordRequestDto();
		dto.setRecordDate(LocalDate.now());
		dto.setWeight(70.0);

		HealthRecord saved = service.createRecord(10L, dto);

		assertEquals(user, saved.getUser());
		assertEquals(70.0, saved.getWeight());
	}

	@Test
    void createRecord_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> service.createRecord(99L, new HealthRecordRequestDto()));
    }

	@Test
	void getRecordsByUserId_returnsList() {
		HealthRecord rec = new HealthRecord();
		rec.setId(1L);
		when(recordRepository.findByUserIdOrderByRecordDateDesc(10L))
				.thenReturn(List.of(rec));

		List<HealthRecord> list = service.getRecordsByUserId(10L);

		assertEquals(1, list.size());
		assertEquals(1L, list.get(0).getId());
	}

	@Test
	void deleteRecordAs_owner_canDelete() {
		User owner = new User();
		owner.setId(1L);
		owner.setRoles(Set.of(Role.ROLE_USER));

		HealthRecord rec = new HealthRecord();
		rec.setId(100L);
		rec.setUser(owner);

		when(recordRepository.findById(100L)).thenReturn(Optional.of(rec));

		assertDoesNotThrow(() -> service.deleteRecordAs(100L, owner));
		verify(recordRepository).delete(rec);
	}

	@Test
	void deleteRecordAs_admin_canDelete() {
		User admin = new User();
		admin.setId(2L);
		admin.setRoles(Set.of(Role.ROLE_ADMIN));

		User other = new User();
		other.setId(3L);

		HealthRecord rec = new HealthRecord();
		rec.setId(200L);
		rec.setUser(other);

		when(recordRepository.findById(200L)).thenReturn(Optional.of(rec));

		assertDoesNotThrow(() -> service.deleteRecordAs(200L, admin));
		verify(recordRepository).delete(rec);
	}

	@Test
	void deleteRecordAs_notOwner_notAdmin_denied() {
		User requester = new User();
		requester.setId(5L);
		requester.setRoles(Set.of(Role.ROLE_USER));

		User owner = new User();
		owner.setId(1L);

		HealthRecord rec = new HealthRecord();
		rec.setId(300L);
		rec.setUser(owner);

		when(recordRepository.findById(300L)).thenReturn(Optional.of(rec));

		assertThrows(AccessDeniedException.class,
				() -> service.deleteRecordAs(300L, requester));
		verify(recordRepository, never()).delete(any());
	}
}
