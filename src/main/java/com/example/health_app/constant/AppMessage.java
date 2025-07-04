package com.example.health_app.constant;

/**
 * メッセージの定数クラス
 */
public class AppMessage {

	// インスタンス化禁止
	private AppMessage() {

	}
	
	// 成功メッセージ
	public static final String USER_CHANGE_PASSWORD = "パスワードを変更しました";
	public static final String RECORD_DELETE = "記録を消去しました";

	// エラーメッセージ
	public static final String EMAIL_ALREADY_USED = "メールアドレスはすでに使用されています。";
	public static final String USER_NOT_FOUND = "ユーザーが見つかりません";
	public static final String RECORD_NOT_FOUND = "記録が存在しません";
	public static final String AUTH_FAILED = "認証に失敗しました";
}
