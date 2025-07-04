package com.example.health_app.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.health_app.constant.SecurityConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * リクエストからJWTによる認証状態を設定する
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader(SecurityConstants.AUTH_HEADER);
		String token = null;
		String username = null;

		// ヘッダーが存在するかつ、ヘッダーの接頭辞が"Bearer "の場合
		if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {

			token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
			username = jwtUtil.extractUsername(token);
		}

		// 認証済み出ない場合
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			// ユーザー名に該当するUserDetailsを取得する
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			// トークン解析結果がtrueの場合
			if (jwtUtil.validateToken(token)) {

				// ユーザー情報を認証済みに変換する
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities());

				// リクエストの詳細情報を設定する
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// ユーザーが認証済みとして登録する
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}

		// 次の処理へ進む
		filterChain.doFilter(request, response);
	}
}
