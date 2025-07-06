package com.example.health_app.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.health_app.constant.AppKeys;
import com.example.health_app.constant.AppMessage;
import com.example.health_app.entity.User;
import com.example.health_app.service.UserService;

import jakarta.persistence.EntityNotFoundException;

/**
 * ユーザー用コントローラー
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * ユーザー作成
	 */
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User user) {

		User saved = userService.registerUser(user);

		return ResponseEntity.ok(saved);
	}

	/**
	 * ユーザー情報取得
	 */
	@GetMapping("/{username}")
	public ResponseEntity<?> getUserByUsername(@PathVariable String username) {

		try {

			User user = userService.findByUsernameOrThrow(username);

			return ResponseEntity.ok(user);

			// ユーザー情報が存在しない場合
		} catch (EntityNotFoundException e) {

			// "ユーザーが見つかりません"
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppMessage.USER_NOT_FOUND);
		}
	}

	/**
	 * ログイン中ユーザー名取得
	 */
	@GetMapping("/me")
	public ResponseEntity<?> getMyInfo(Principal principal) {

		try {

			// ログイン中のユーザー名を取得する
			User user = userService.findByUsernameOrThrow(principal.getName());

			return ResponseEntity.ok(user);

		} catch (EntityNotFoundException e) {

			// "ユーザーが見つかりません"
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppMessage.USER_NOT_FOUND);
		}

	}

	/**
	 * パスワード変更
	 */
	@PutMapping("/{id}/password")
	public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {

		// 入力パスワードを取得する
		String newPassword = body.get(AppKeys.NEW_PASSWORD);

		userService.changePassword(id, newPassword);

		// "パスワードを変更しました"
		return ResponseEntity.ok(AppMessage.USER_CHANGE_PASSWORD);
	}
}
