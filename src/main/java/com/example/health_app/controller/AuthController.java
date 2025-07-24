package com.example.health_app.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.health_app.constant.AppKeys;
import com.example.health_app.constant.AppMessage;
import com.example.health_app.entity.Role;
import com.example.health_app.entity.User;
import com.example.health_app.security.JwtUtil;
import com.example.health_app.service.UserService;

/**
 * ログイン処理用コントローラー
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final JwtUtil jwtUtil;

	public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.userService = userService;
		this.jwtUtil = jwtUtil;
	}

	/**
	 * ユーザー名とパスワードでユーザーを認証し、JWTトークンを返却する
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> body) {

		try {

			String username = body.get(AppKeys.USERNAME);
			String password = body.get(AppKeys.PASSWORD);

			// ユーザー名とパスワードで認証処理
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(username, password));

			User user = userService.findByUsernameOrThrow(username);
			
			Role role = user.getRoles().stream().findFirst().orElse(Role.ROLE_USER);
			String token = jwtUtil.generateToken(user.getUsername(), role);

			Map<String, String> response = new HashMap<>();

			response.put(AppKeys.JWT_TOKEN, token);

			return ResponseEntity.ok(response);

		} catch (AuthenticationException e) {

			// "認証に失敗しました"
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AppMessage.AUTH_FAILED);
		}
	}
}
