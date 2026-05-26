package com.example.blog.admin;

import com.example.blog.admin.dto.AdminUserItemResponse;
import com.example.blog.admin.dto.AdminUserResponse;
import com.example.blog.admin.dto.CreateAuthorRequest;
import com.example.blog.admin.dto.UpdateUserStatusRequest;
import com.example.blog.common.PageResponse;
import com.example.blog.exception.ConflictException;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.User;
import com.example.blog.model.UserRole;
import com.example.blog.model.UserStatus;
import com.example.blog.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserItemResponse> listAuthors(int page, int size) {
        return PageResponse.from(userRepository.findByRole(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")),
                UserRole.AUTHOR
        ).map(this::toItemResponse));
    }

    @Transactional
    public AdminUserResponse createAuthor(CreateAuthorRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("用户名已存在: " + username);
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname().trim());
        user.setEmail(normalize(request.email()));
        user.setRole(UserRole.AUTHOR);
        user.setStatus(UserStatus.ENABLED);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = userRepository.findById(id)
                .filter(value -> value.getRole() == UserRole.AUTHOR)
                .orElseThrow(() -> new ResourceNotFoundException("作者不存在: " + id));

        user.setStatus(Boolean.TRUE.equals(request.enabled()) ? UserStatus.ENABLED : UserStatus.DISABLED);
        user.setUpdatedAt(LocalDateTime.now());
        return toUserResponse(userRepository.save(user));
    }

    private AdminUserItemResponse toItemResponse(User user) {
        return new AdminUserItemResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private AdminUserResponse toUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
