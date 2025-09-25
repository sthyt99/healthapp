package com.example.health_app.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.health_app.dto.DashboardDto;
import com.example.health_app.dto.UserDto;
import com.example.health_app.entity.Role;
import com.example.health_app.entity.User;
import com.example.health_app.repository.HealthRecordRepository;
import com.example.health_app.repository.UserRepository;

/**
 * 管理者のサービス層
 */
@Service
public class AdminService {

	private final UserRepository userRepository;
	private final HealthRecordRepository healthRecordRepository;

	public AdminService(UserRepository userRepository, HealthRecordRepository healthRecordRepository) {
		this.userRepository = userRepository;
		this.healthRecordRepository = healthRecordRepository;
	}

	/**
	 * 全ユーザーをDtoで返却
	 */
	public List<UserDto> getAllUsers() {
		return userRepository.findAll().stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	/**
	 * 統計データ作成
	 */
	public DashboardDto getDashboardData() {
		long totalUsers = userRepository.count();
		long adminCount = userRepository.countByRole(Role.ROLE_ADMIN);
		long totalRecords = healthRecordRepository.count();
		long todayActiveUsers = healthRecordRepository.countDistinctUsersByRecordDate(LocalDate.now());

		return new DashboardDto(totalUsers, adminCount, totalRecords, todayActiveUsers);
	}

	/**
	 * UserDtoに変換
	 */
	private UserDto toDto(User user) {
		Set<String> roles = user.getRoles().stream()
				.map(Enum::name)
				.collect(Collectors.toSet());

		return new UserDto(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getAge(),
				user.getGender(),
				roles);
	}
}
