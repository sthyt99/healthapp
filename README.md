# 健康記録アプリ - HealthApp


## 概要
このアプリは、ユーザーが日々の健康データ（体重・血圧・体温・歩数など）を記録・管理できるWebアプリです。Spring BootとMySQLを使用してバックエンドを構築しています。


## 使用技術
- Java 17
- Spring Boot 3.5.3
- Spring Data JPA
- Spring Security (JWT)
- MySQL
- Maven


## 機能一覧
- ユーザー登録・ログイン（JWT認証）
- 健康記録の作成・取得・更新・削除（CRUD）
- ユーザーごとの記録の履歴表示
- 管理者によるユーザー一覧表示（オプション）


## テスト
- JUnit5 / Mockito による単体テスト
- テストカバレッジ 50％以上を目標


## セットアップ手順

1. このリポジトリをクローン：
   ```bash
   git clone https://github.com/yourname/healthapp.git
   cd healthapp
2. MySQLにデータベースを作成：
   ```bash
   CREATE DATABASE healthapp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
3. application.properties にDB設定を記述：
   ```bash
   spring.datasource.url=jdbc:mysql://localhost:3306/healthapp_db
   spring.datasource.username=youruser
   spring.datasource.password=yourpass
4. アプリ起動：
   ```bash
   mvn spring-boot:run

## API 仕様書

| メソッド | パス                    | 内容          |
| ---- | --------------------- | ----------- |
| POST | `/api/users/register` | ユーザー登録      |
| POST | `/api/users/login`    | ログイン（JWT取得） |
| GET  | `/api/records`        | 自分の健康記録一覧取得 |
| POST | `/api/records`        | 新規健康記録の登録   |


## ディレクトリ構成

```text
src/
├── main/
│   ├── java/com/example/healthapp/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── service/
│   │   └── security/
│   └── resources/
│       └── application.properties
