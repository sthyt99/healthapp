package com.example.health_app.e2e;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    String baseUrl() { return "http://localhost:" + port; }

    @BeforeEach
    void setUpUser() {
        userRepository.deleteAll();
        User u = new User();
        u.setUsername("taro");
        u.setEmail("taro@example.com");
        u.setPassword(passwordEncoder.encode("Passw0rd!")); // 認証に必要
        u.setAge(30);
        u.setGender("MALE");            // enumなら実型に合わせて
        u.setRoleVersion(0L);
        u.setRoles(Set.of(Role.ROLE_USER));
        userRepository.save(u);
    }

    private String loginAndGetBearer() {
        var body = Map.of("username", "taro", "password", "Passw0rd!");
        var resp = rest.postForEntity(baseUrl() + "/api/auth/login", body, Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) resp.getBody().get("token");
        return "Bearer " + token;
    }

    @Test
    void fullFlow_login_create_get_delete_record() {
        String bearer = loginAndGetBearer();

        // 1) create my record
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Authorization", bearer);

        String createJson = """
            {
              "recordDate": "2025-01-02",
              "weight": 65.2,
              "systolic": 118,
              "diastolic": 76,
              "bodyTemperature": 36.4,
              "steps": 8000
            }
            """;

        var createResp = rest.exchange(
                baseUrl() + "/api/records/me",
                HttpMethod.POST,
                new HttpEntity<>(createJson, h),
                Map.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Number recordId = (Number) createResp.getBody().get("id");
        assertThat(recordId.longValue()).isPositive();

        // 2) get my records
        ResponseEntity<List<Map<String,Object>>> listResp = rest.exchange(
                baseUrl() + "/api/records/me",
                HttpMethod.GET,
                new HttpEntity<>(h),
                new org.springframework.core.ParameterizedTypeReference<List<Map<String,Object>>>() {}
        );

        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody()).isNotEmpty();

        // 3) delete my record
        var delResp = rest.exchange(
                baseUrl() + "/api/records/" + recordId.longValue(),
                HttpMethod.DELETE,
                new HttpEntity<>(h),
                String.class);

        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        // メッセージ固定ならここで body の一致もチェック
    }
}
