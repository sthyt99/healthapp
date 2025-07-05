package com.example.health_app.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.health_app.entity.Role;
import com.example.health_app.entity.User;
import com.example.health_app.repository.UserRepository;

/**
 * 初期データ登録
 */
@Component
public class DataInitializer implements CommandLineRunner {

	/** 管理者 */
	/** メールアドレス */
	private static final String ADMIN_EMAIL = "admin@example.com";
	/** ユーザー名 */
	private static final String ADMIN_USERNAME = "admin";
	/** パスワード */
	private static final String ADMIN_PASSWORD = "adminpass";

	/** 一般ユーザー */
	/** メールアドレス */
	private static final String USER_EMAIL = "user@example.com";
	/** ユーザー名 */
	private static final String USER_USERNAME = "user";
	/** パスワード */
	private static final String USER_PASSWORD = "userpass";
	
	/** */
	private static final String MESSAGE_SUCCESS = "初期ユーザー登録完了";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {

		// 管理者の初期データ
		if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
			User admin = new User();
			admin.setUsername(ADMIN_USERNAME);
			admin.setEmail(ADMIN_EMAIL);
			admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
			admin.setRoles(Set.of(Role.ROLE_ADMIN));
			userRepository.save(admin);
		}

		// 一般ユーザーの初期データ
		if (userRepository.findByEmail(USER_EMAIL).isEmpty()) {
			User user = new User();
			user.setUsername(USER_USERNAME);
			user.setEmail(USER_EMAIL);
			user.setPassword(passwordEncoder.encode(USER_PASSWORD));
			user.setRoles(Set.of(Role.ROLE_USER));
			userRepository.save(user);
		}

		// "初期ユーザー登録完了"
		System.out.println(MESSAGE_SUCCESS);
	}
}
