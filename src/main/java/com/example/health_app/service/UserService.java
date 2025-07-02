package com.example.health_app.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.health_app.constant.AppMessage;
import com.example.health_app.entity.User;
import com.example.health_app.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * ユーザーのサービス層
 */
@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * ユーザー作成処理
	 * 
	 * @param user ユーザー情報
	 * @return ユーザー情報を保存する
	 */
	public User registerUser(User user) {

		// 既存ユーザーが存在する場合
		if (userRepository.findByEmail(user.getEmail()).isPresent()) {

			throw new IllegalArgumentException(AppMessage.EMAIL_ALREADY_USED);
		}

		// パスワードをハッシュ化する
		String encodedPassword = passwordEncoder.encode(user.getPassword());

		// ハッシュ化したパスワードを設定する
		user.setPassword(encodedPassword);

		return userRepository.save(user);
	}

	/**
	 * メールアドレス検索処理
	 * 
	 * @param email メールアドレス
	 * @return メールアドレスを検索し、ユーザー情報を取得する
	 */
	public Optional<User> findByEmail(String email) {

		return userRepository.findByEmail(email);
	}

	/**
	 * ユーザー名検索処理
	 * 
	 * @param username ユーザー名
	 * @return ユーザー名を検索し、ユーザー情報を取得する。無ければ、例外をスローする
	 */
	public User findByUsernameOrThrow(String username) {

		return userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException(AppMessage.USER_NOT_FOUND));
	}

	/**
	 * パスワード変更処理
	 * 
	 * @param userid ユーザーID
	 * @param newPassword 新パスワード
	 */
	public void changePassword(Long userId, String newPassword) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException(AppMessage.USER_NOT_FOUND));

		// パスワードをハッシュ化する
		String encoded = passwordEncoder.encode(newPassword);

		// ハッシュ化したパスワードを設定する
		user.setPassword(encoded);

		userRepository.save(user);
	}
}
