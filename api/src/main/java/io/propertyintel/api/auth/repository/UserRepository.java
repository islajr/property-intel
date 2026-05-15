package io.propertyintel.api.auth.repository;

import io.propertyintel.api.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByEmail(String username);

    boolean existsByEmail(String email);
}
