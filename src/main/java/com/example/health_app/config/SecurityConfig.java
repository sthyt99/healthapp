package com.example.health_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.health_app.security.JwtAuthenticationFilter;
import com.example.health_app.security.UserDetailsServiceImpl;

/**
 * Spring Boot アプリケーションにおける認証・認可のルールや、フィルターの追加、
 * パスワードエンコーダの定義などを行うクラス
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;
	private final UserDetailsServiceImpl userDetailsService;

	public SecurityConfig(JwtAuthenticationFilter jwtFilter, UserDetailsServiceImpl userDetailsService) {
		this.jwtFilter = jwtFilter;
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		return http
				.csrf(csrf -> csrf.disable()) // CSRF無効化
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // セッション未使用
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/login", "/api/users/register").permitAll() // 認証不要
						.anyRequest().authenticated() // それ以外は認証必須
				)
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // ユーザー名、パスワード認証前にJWT確認
				.userDetailsService(userDetailsService) // ユーザー情報読み込み
				.build();
	}

	/**
	 * 認証マネージャーを取得する
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		
		return config.getAuthenticationManager();
	}

	/**
	 * パスワードを暗号化する
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();
	}
}
