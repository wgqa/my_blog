package com.example.blog.repository;

import com.example.blog.model.User;
import com.example.blog.model.UserRole;
import com.example.blog.model.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByRole(UserRole role);

    long countByStatus(UserStatus status);

    long countByRole(UserRole role);

    Optional<User> findByUsernameAndRoleAndStatus(String username, UserRole role, UserStatus status);

    Page<User> findByRole(Pageable pageable, UserRole role);
}
