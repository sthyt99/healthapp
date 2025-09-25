package com.example.health_app.controller;

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
	public ResponseEntity<UserDto> registerUser(@RequestBody User user) {

		User saved = userService.registerUser(user);

		return ResponseEntity.ok(toDto(saved));
	}

	/**
	 * 指定ユーザー情報（管理用）
	 */
	@PreAuthorize("hasRole('ADMIN')")
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

		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AppMessage.AUTH_REQUIRED);
		}

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
	public ResponseEntity<?> changePasswordBySelf(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody Map<String, String> body) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AppMessage.AUTH_REQUIRED);
		}

		String newPassword = body.get(AppKeys.NEW_PASSWORD);
		if (newPassword == null || newPassword.length() < AppConstraints.PASSWORD_MIN_LENGTH) {
			return ResponseEntity.badRequest()
					.body(String.format(AppMessage.PASSWORD_TOO_SHORT, AppConstraints.PASSWORD_MIN_LENGTH));
		}

		try {
			User me = userService.findByUsernameOrThrow(userDetails.getUsername());
			userService.changePassword(me.getId(), newPassword);
			return ResponseEntity.ok(AppMessage.USER_CHANGE_PASSWORD);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppMessage.USER_NOT_FOUND);
		}
	}

	/**
	 * 管理者用パスワード変更
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}/password")
	public ResponseEntity<?> changePasswordByAdmin(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String newPassword = body.get(AppKeys.NEW_PASSWORD);
        if (newPassword == null || newPassword.length() < AppConstraints.PASSWORD_MIN_LENGTH) {
            return ResponseEntity.badRequest()
                    .body(String.format(AppMessage.PASSWORD_TOO_SHORT, AppConstraints.PASSWORD_MIN_LENGTH));
        }
        userService.changePassword(id, newPassword);
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
