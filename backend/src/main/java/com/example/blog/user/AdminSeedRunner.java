package com.example.blog.user;

import com.example.blog.model.User;
import com.example.blog.model.UserRole;
import com.example.blog.model.UserStatus;
import com.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminSeedRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String nickname;

    public AdminSeedRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${admin.seed.username:admin}") String username,
            @Value("${admin.seed.password:admin123456}") String password,
            @Value("${admin.seed.nickname:管理员}") String nickname
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        User admin = new User();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setNickname(nickname);
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ENABLED);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        userRepository.save(admin);
    }
}
