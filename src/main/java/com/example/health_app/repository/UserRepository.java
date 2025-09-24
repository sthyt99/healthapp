package com.example.health_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.health_app.entity.Role;
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
	
	@Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
	long countByRole(@Param("role") Role role);

}
