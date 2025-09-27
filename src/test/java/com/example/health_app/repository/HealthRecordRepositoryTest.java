package com.example.health_app.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.example.health_app.entity.HealthRecord;
import com.example.health_app.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY) // 組み込みDB(H2)に置換
@TestPropertySource(properties = {
    "spring.flyway.enabled=false" 
})
class HealthRecordRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired HealthRecordRepository repo;

    private User persistUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(username + "@example.com");

        // ↓↓↓ Userエンティティの必須項目に合わせて適宜追加してください ↓↓↓
        u.setPassword("Passw0rd!");            // @NotBlank 等があるなら
        u.setAge(30);                          // 型に合わせる
        u.setGender("MALE");                   // enumなら実型に合わせる
        u.setRoleVersion(0L);                  // long型なら0など
        u.setRoles(Set.of());                  // 役割が必須なら Set.of(Role.USER) 等
        // ↑↑↑ 必須でないなら削ってOK ↑↑↑

        em.persist(u);
        return u;
    }

    private HealthRecord persistRecord(User u, LocalDate date) {
        HealthRecord r = new HealthRecord();
        r.setUser(u);
        r.setRecordDate(date);
        r.setWeight(60.0);
        r.setSystolic(120);
        r.setDiastolic(80);
        r.setBodyTemperature(36.5);
        r.setSteps(5000);
        em.persist(r);
        return r;
    }

    @Test
    void findByUserId_returnsRecordsForUser() {
        User u1 = persistUser("taro");
        User u2 = persistUser("hanako");

        persistRecord(u1, LocalDate.of(2024, 1, 1));
        persistRecord(u2, LocalDate.of(2024, 1, 2));

        em.flush(); em.clear();

        List<HealthRecord> result = repo.findByUserId(u1.getId()); // ★修正
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("taro");
    }

    @Test
    void findByUserIdOrderByRecordDateDesc_returnsSortedRecords() {
        User u = persistUser("taro");
        persistRecord(u, LocalDate.of(2024, 1, 1));
        persistRecord(u, LocalDate.of(2024, 1, 3));
        persistRecord(u, LocalDate.of(2024, 1, 2));

        em.flush(); em.clear();

        List<HealthRecord> result = repo.findByUserIdOrderByRecordDateDesc(u.getId()); // ★修正
        assertThat(result).extracting(HealthRecord::getRecordDate)
                          .containsExactly(LocalDate.of(2024, 1, 3),
                                           LocalDate.of(2024, 1, 2),
                                           LocalDate.of(2024, 1, 1));
    }

    @Test
    void findByRecordDate_returnsOnlyMatchingDate() {
        User u = persistUser("taro");
        persistRecord(u, LocalDate.of(2024, 1, 1));
        persistRecord(u, LocalDate.of(2024, 1, 2));

        em.flush(); em.clear();

        List<HealthRecord> result = repo.findByRecordDate(LocalDate.of(2024, 1, 2));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecordDate()).isEqualTo(LocalDate.of(2024, 1, 2));
    }

    @Test
    void countDistinctUsersByRecordDate_returnsUniqueUserCount() {
        User u1 = persistUser("taro");
        User u2 = persistUser("hanako");

        persistRecord(u1, LocalDate.of(2024, 1, 1));
        persistRecord(u1, LocalDate.of(2024, 1, 1)); // 同ユーザーで二本
        persistRecord(u2, LocalDate.of(2024, 1, 1));

        em.flush(); em.clear();

        long count = repo.countDistinctUsersByRecordDate(LocalDate.of(2024, 1, 1));
        assertThat(count).isEqualTo(2);
    }
}
