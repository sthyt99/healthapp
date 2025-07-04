package com.example.health_app.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理者用コントローラー
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/dashboard")
	public String adminOnly() {

		return "管理者専用のダッシュボードです";
	}
}
