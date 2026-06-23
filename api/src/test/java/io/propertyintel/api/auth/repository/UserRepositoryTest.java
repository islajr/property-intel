package io.propertyintel.api.auth.repository;

import io.propertyintel.api.BaseDatabaseTest;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends BaseDatabaseTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUserByEmail() {
        User user = User.builder()
                .email("test@example.com")
                .password("encoded-pwd")
                .role("USER")
                .isEmailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());

        Optional<User> foundUser = userRepository.findUserByEmail("test@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("encoded-pwd", foundUser.get().getPassword());
        assertTrue(foundUser.get().getIsEmailVerified());
    }

    @Test
    void testExistsByEmail() {
        User user = User.builder()
                .email("exists@example.com")
                .password("pwd")
                .role("USER")
                .isEmailVerified(false)
                .userStatus(UserStatus.UNVERIFIED)
                .build();

        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("exists@example.com"));
        assertFalse(userRepository.existsByEmail("notexists@example.com"));
    }

    @Test
    void testUniqueEmailConstraint() {
        User user1 = User.builder()
                .email("duplicate@example.com")
                .password("pwd")
                .role("USER")
                .isEmailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();

        User user2 = User.builder()
                .email("duplicate@example.com")
                .password("pwd")
                .role("USER")
                .isEmailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();

        userRepository.saveAndFlush(user1);

        assertThrows(DataIntegrityViolationException.class, () -> 
                userRepository.saveAndFlush(user2)
        );
    }
}
