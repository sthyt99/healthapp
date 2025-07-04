package com.example.health_app.constant;

/**
 * JWTの定数クラス
 */
public class SecurityConstants {

	// インスタンス禁止
	private SecurityConstants() {

	}

	// JWT関連
	public static final String SECRET_KEY = "your-256-bit-secret-your-256-bit-secret"; // 秘密鍵の文字列(32文字以上)
	public static final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 有効期限 : 24時間

	// ヘッダー関連
	public static final String AUTH_HEADER = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";
}
