package com.example.health_app.controller;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.health_app.constant.AppConstraints;
import com.example.health_app.constant.AppKeys;
import com.example.health_app.constant.AppMessage;
import com.example.health_app.dto.UserDto;
import com.example.health_app.entity.User;
import com.example.health_app.security.CustomUserDetails;
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

		return ResponseEntity.ok(toDto(saved));
	}

	/**
	 * 指定ユーザー情報を取得（管理画面等）
	 */
	@GetMapping("/{username}")
	public ResponseEntity<?> getUserByUsername(@PathVariable String username) {

		try {

			User user = userService.findByUsernameOrThrow(username);

			return ResponseEntity.ok(toDto(user));

			// ユーザー情報が存在しない場合
		} catch (EntityNotFoundException e) {

			// "ユーザーが見つかりません"
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppMessage.USER_NOT_FOUND);
		}
	}

	/**
	 * ログイン中ユーザー情報取得
	 */
	@GetMapping("/me")
	public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

		try {

			// ログイン中のユーザー情報を取得する
			User user = userService.findByUsernameOrThrow(userDetails.getUsername());

			// DTO化
			return ResponseEntity.ok(toDto(user));

		} catch (EntityNotFoundException e) {

			// "ユーザーが見つかりません"
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppMessage.USER_NOT_FOUND);
		}
	}

	/**
	 * ユーザー用パスワード変更
	 */
	@PutMapping("/password")
	public ResponseEntity<?> changePasswordBySelf(Principal principal, @RequestBody Map<String, String> body) {

		// 入力新パスワード
		String newPassword = body.get(AppKeys.NEW_PASSWORD);

		// 新パスワードがないまたは、パスワードの文字数が6より小さい場合
		if (newPassword == null || newPassword.length() < AppConstraints.PASSWORD_MIN_LENGTH) {

			// "新しいパスワードは6文字以上である必要があります。"
			return ResponseEntity.badRequest()
					.body(String.format(AppMessage.PASSWORD_TOO_SHORT, AppConstraints.PASSWORD_MIN_LENGTH));
		}

		// ログイン済みユーザー情報を取得する
		User user = userService.findByUsernameOrThrow(principal.getName());

		// パスワード変更処理
		userService.changePassword(user.getId(), newPassword);

		// "パスワードを変更しました"
		return ResponseEntity.ok(AppMessage.USER_CHANGE_PASSWORD);
	}

	/**
	 * 管理者用パスワード変更
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}/password")
	public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {

		// 入力パスワードを取得する
		String newPassword = body.get(AppKeys.NEW_PASSWORD);

		// 新パスワードがないまたは、パスワードの文字数が6より小さい場合
		if (newPassword == null || newPassword.length() < AppConstraints.PASSWORD_MIN_LENGTH) {

			// "新しいパスワードは6文字以上である必要があります。"
			return ResponseEntity.badRequest()
					.body(String.format(AppMessage.PASSWORD_TOO_SHORT, AppConstraints.PASSWORD_MIN_LENGTH));
		}

		// パスワード変更処理
		userService.changePassword(id, newPassword);

		// "パスワードを変更しました"
		return ResponseEntity.ok(AppMessage.USER_CHANGE_PASSWORD);
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
