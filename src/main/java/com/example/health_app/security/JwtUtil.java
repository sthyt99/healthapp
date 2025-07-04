package com.example.health_app.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.example.health_app.constant.SecurityConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JWTトークン生成・検証
 */
@Component
public class JwtUtil {

	// Key 型に変換し、署名用に利用
	private final Key secretKey = Keys.hmacShaKeyFor(SecurityConstants.SECRET_KEY.getBytes());

	/**
	 * JWTを発行する(トークンを生成)
	 */
	public String generateToken(String username) {

		// ユーザー名、発行日時、有効期限、署名をJWT文字列に変換し、返却する
		return Jwts.builder()
				.setSubject(username) // トークンの対象（ユーザー名)
				.setIssuedAt(new Date()) // 発行時刻
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_MS)) // 24時間有効
				.signWith(secretKey, SignatureAlgorithm.HS256) // 署名
				.compact(); // JWT文字列に変換
	}

	/**
	 * トークン解析
	 */
	public String extractUsername(String token) {

		return this.parseClaims(token).getSubject();
	}

	/**
	 * トークン検証
	 */
	public boolean validateToken(String token) {

		try {

			// トークン解析処理
			this.parseClaims(token);

			return true;

			// トークンが不正の場合
		} catch (JwtException e) {

			return false;
		}
	}

	/**
	 * トークンをパースしてClaims（中身）を返す
	 */
	private Claims parseClaims(String token) {

		return Jwts.parserBuilder() // JWT を解析（パース）するためのビルダーを取得
				.setSigningKey(secretKey) // 検証に使用するキー
				.build() // 上記キーをもとにインスタンス生成
				.parseClaimsJws(token) // トークンを解析
				.getBody(); // ペイロード（トークンの中身）を取得
	}
}
