package com.example.health_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.health_app.entity.User;

/**
 * ユーザー用リポジトリ
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * メールアドレス検索
	 */
	Optional<User> findByEmail(String email);

	/**
	 * ユーザー名検索
	 */
	Optional<User> findByUsername(String username);
}
