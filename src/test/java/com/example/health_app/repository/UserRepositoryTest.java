package com.example.health_app.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.example.health_app.entity.Role;
import com.example.health_app.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)   // H2 に置換
@TestPropertySource(properties = {
    "spring.flyway.enabled=false"
})
class UserRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired UserRepository userRepository;

    private User persistUser(String username, String email, Set<Role> roles) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("Passw0rd!");
        u.setRoleVersion(0L);
        u.setRoles(roles);
        em.persist(u);
        return u;
    }

    @Test
    void findByEmail_returnsUser() {
        persistUser("taro", "taro@example.com", Set.of(Role.ROLE_USER));
        em.flush(); em.clear();

        var found = userRepository.findByEmail("taro@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("taro");
    }

    @Test
    void findByUsername_returnsUser() {
        persistUser("hanako", "hanako@example.com", Set.of(Role.ROLE_USER));
        em.flush(); em.clear();

        var found = userRepository.findByUsername("hanako");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("hanako@example.com");
    }

    @Test
    void countByRole_countsUsersHavingGivenRole() {
        persistUser("admin1", "a1@example.com", Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
        persistUser("admin2", "a2@example.com", Set.of(Role.ROLE_ADMIN));
        persistUser("user1",  "u1@example.com", Set.of(Role.ROLE_USER));
        em.flush(); em.clear();

        long admins = userRepository.countByRole(Role.ROLE_ADMIN);
        long users  = userRepository.countByRole(Role.ROLE_USER);

        assertThat(admins).isEqualTo(2);
        assertThat(users).isEqualTo(2);
    }
}
