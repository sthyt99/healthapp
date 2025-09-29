package com.example.health_app.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
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

	/** ロールバージョン無し */
	private static final long RV_UNKNOWN = -1L;
	private static final String RV_MISMATCH = "Role version mismatch";

	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String uri = request.getRequestURI();
		boolean adminPath = uri.startsWith("/api/admin"); // 管理系パス判定
		String authHeader = request.getHeader(SecurityConstants.AUTH_HEADER);

		// ヘッダーが存在するかつ、ヘッダーの接頭辞が"Bearer "の場合
		if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {

			String token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());

			// トークン解析結果がtrueの場合
			if (jwtUtil.validateToken(token)) {

				String username = jwtUtil.extractUsername(token);

				// ユーザー情報をロード（最低限の存在確認とDBロール参照用）
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				Collection<? extends GrantedAuthority> authorities = resolveAuthorities(userDetails, token, adminPath);

				UsernamePasswordAuthenticationToken auth =
	                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
	            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

	            // SecurityContext を明示的に生成・保存
	            SecurityContext context = SecurityContextHolder.createEmptyContext();
	            context.setAuthentication(auth);
	            SecurityContextHolder.setContext(context);
			}
		}

		// 次の処理へ進む
		filterChain.doFilter(request, response);
	}

	/**
	 * 認証判定
	 */
	private Collection<? extends GrantedAuthority> resolveAuthorities(UserDetails userDetails, String token,
			boolean adminPath) {
		if (adminPath) {
			long jwtRv = jwtUtil.extractRoleVersion(token);
			long dbRv = (userDetails instanceof CustomUserDetails cud) ? cud.getUser().getRoleVersion() : RV_UNKNOWN;
			if (jwtRv != dbRv)
				throw new AccessDeniedException(RV_MISMATCH);
			return userDetails.getAuthorities();
		} else {
			return jwtUtil.extractRoles(token).stream()
					.map(SimpleGrantedAuthority::new)
					.toList();
		}
	}

}
