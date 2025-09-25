package com.example.health_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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

import jakarta.servlet.http.HttpServletResponse;

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
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
            )
            .authorizeHttpRequests(auth -> auth
                // プリフライト
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 管理系
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 認証不要エンドポイント（実際のURLに合わせて！）
                .requestMatchers("/api/auth/login", "/api/users/register").permitAll()
                // それ以外は認証必須
                .anyRequest().authenticated()
            )
            // Username/Password 認証より前に JWT フィルタを入れる
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            // UserDetailsService を使う
            .userDetailsService(userDetailsService)
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

