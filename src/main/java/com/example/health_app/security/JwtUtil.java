package com.example.health_app.security;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.health_app.constant.SecurityConstants;
import com.example.health_app.entity.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;


/**
 * JWTトークン生成・検証
 */
@Component
public class JwtUtil {

	// Key 型に変換し、署名用に利用
	private final Key secretKey = Keys.hmacShaKeyFor(SecurityConstants.SECRET_KEY.getBytes());
	
	// 数十秒の許容（リバースプロキシ等の時刻差対策）
    private static final long ALLOWED_CLOCK_SKEW_SEC = 60;

	/**
	 * JWTを発行する(トークンを生成)
	 */
	public String generateToken(String username, Set<Role> roles, long roleVersion) {
		
		// 複数ロールに対応するロール名
		List<String> roleNames = roles.stream().map(Enum::name).collect(Collectors.toList());
		
		long now = System.currentTimeMillis();

		// ユーザー名、発行日時、有効期限、署名をJWT文字列に変換し、返却する
		return Jwts.builder()
				.setSubject(username) // トークンの対象（ユーザー名)
				.claim("roles", roleNames) // ロール
				.claim("rv", roleVersion) // roleVersion
				.setIssuedAt(new Date(now)) // 発行時刻
				.setExpiration(new Date(now + SecurityConstants.EXPIRATION_MS)) // 24時間有効
				.signWith(secretKey, SignatureAlgorithm.HS256) // 署名
				.compact(); // JWT文字列に変換
	}

	/**
	 * トークン解析
	 */
	public String extractUsername(String token) {

		Claims c = parseClaims(token);
		
		return c != null ? c.getSubject() : null;
	}
	
	 /**
     * 役割（rolesクレーム）を抽出（型安全）
     */
    public List<String> extractRoles(String token) {
        Claims c = parseClaims(token);
        List<?> raw = (c == null) ? null : c.get("roles", List.class); // 生のListで受ける
        return (raw == null) ? List.of() : raw.stream().map(Object::toString).collect(Collectors.toList());
    }
    
    /**
     * ロールバージョンを抽出
     */
    public long extractRoleVersion(String token) {
        Claims c = parseClaims(token);
        if (c == null) return -1;
        Object v = c.get("rv");
        return (v instanceof Number) ? ((Number) v).longValue() : -1;
    }

    /**
     * トークン検証：署名・期限・形式など
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            // 解析に成功すれば基本OK（有効期限・署名含む）
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 有効期限切れ
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException e) {
            // 形式不正・アルゴリズム不一致・署名不正
            return false;
        } catch (IllegalArgumentException e) {
            // 空トークン等
            return false;
        }
    }

	/**
	 * トークンをパースしてClaims（中身）を返す
	 */
	public Claims parseClaims(String token) {
		
		if (token == null || token.isBlank()) return null;

		return Jwts.parserBuilder() // JWT を解析（パース）するためのビルダーを取得
				.setSigningKey(secretKey) // 検証に使用するキー
				.setAllowedClockSkewSeconds(ALLOWED_CLOCK_SKEW_SEC)
				.build() // 上記キーをもとにインスタンス生成
				.parseClaimsJws(token) // トークンを解析
				.getBody(); // ペイロード（トークンの中身）を取得
	}
}
