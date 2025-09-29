package com.example.health_app.e2e;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.health_app.entity.Role;
import com.example.health_app.entity.User;
import com.example.health_app.repository.UserRepository;

/**
 * 管理者フローとアクセス制御のE2E統合テスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminApiE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void resetUsers() {
        userRepository.deleteAll();

        // 管理者
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("AdminPass!"));
        admin.setAge(40);
        admin.setGender("MALE");
        admin.setRoleVersion(0L);
        admin.setRoles(Set.of(Role.ROLE_ADMIN));
        userRepository.save(admin);

        // 一般ユーザー
        User user = new User();
        user.setUsername("taro");
        user.setEmail("taro@example.com");
        user.setPassword(passwordEncoder.encode("UserPass!"));
        user.setAge(25);
        user.setGender("MALE");
        user.setRoleVersion(0L);
        user.setRoles(Set.of(Role.ROLE_USER));
        userRepository.save(user);
    }

    private String login(String username, String password) {
        var body = Map.of("username", username, "password", password);
        ResponseEntity<Map> resp = rest.postForEntity(baseUrl() + "/api/auth/login", body, Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return "Bearer " + (String) resp.getBody().get("token");
    }

    @Test
    void admin_can_create_and_delete_record_for_user() {
        // 管理者ログイン
        String bearer = login("admin", "AdminPass!");

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Authorization", bearer);

        // 任意ユーザー(taro)のIDを取得
        User target = userRepository.findByUsername("taro").orElseThrow();

        // 1) 管理者が taro の記録を作成
        String createJson = """
            {
              "recordDate": "2025-01-02",
              "weight": 70.0,
              "systolic": 120,
              "diastolic": 80,
              "bodyTemperature": 36.5,
              "steps": 6000
            }
            """;

        ResponseEntity<Map> createResp = rest.exchange(
                baseUrl() + "/api/admin/records/" + target.getId(),
                HttpMethod.POST,
                new HttpEntity<>(createJson, h),
                Map.class
        );

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Number recordId = (Number) createResp.getBody().get("id");
        assertThat(recordId.longValue()).isPositive();

        // 2) 管理者が taro の記録一覧を取得
        ResponseEntity<java.util.List> listResp = rest.exchange(
                baseUrl() + "/api/admin/records/" + target.getId(),
                HttpMethod.GET,
                new HttpEntity<>(h),
                java.util.List.class
        );
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody()).isNotEmpty();

        // 3) 管理者が taro の記録を削除
        ResponseEntity<String> delResp = rest.exchange(
                baseUrl() + "/api/admin/records/" + recordId.longValue(),
                HttpMethod.DELETE,
                new HttpEntity<>(h),
                String.class
        );
        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void normal_user_cannot_access_admin_endpoints() {
        // 一般ユーザーでログイン
        String bearer = login("taro", "UserPass!");

        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", bearer);

        // 管理系APIへアクセス
        ResponseEntity<String> resp = rest.exchange(
                baseUrl() + "/api/admin/records/1",
                HttpMethod.GET,
                new HttpEntity<>(h),
                String.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
