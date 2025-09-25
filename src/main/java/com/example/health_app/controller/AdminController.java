package com.example.health_app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.health_app.dto.DashboardDto;
import com.example.health_app.dto.UserDto;
import com.example.health_app.service.AdminService;

/**
 * 管理者用コントローラー
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	/**
	 * 全ユーザーの一覧を取得
	 */
	@GetMapping("/users")
	public ResponseEntity<List<UserDto>> getAllUsers() {
		List<UserDto> users = adminService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	/**
	 * ダッシュボードの統計データを取得
	 */
	@GetMapping("/dashboard")
	public ResponseEntity<DashboardDto> getDashboardData() {

		DashboardDto dashboard = adminService.getDashboardData();
		return ResponseEntity.ok(dashboard);
	}
}
