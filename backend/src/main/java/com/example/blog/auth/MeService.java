package com.example.blog.auth;

import com.example.blog.auth.dto.MeProfileResponse;
import com.example.blog.auth.dto.UpdateMeProfileRequest;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MeService {

    private final UserRepository userRepository;

    public MeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MeProfileResponse getProfile() {
        User user = getCurrentUser();
        return toResponse(user);
    }

    @Transactional
    public MeProfileResponse updateProfile(UpdateMeProfileRequest request) {
        User user = getCurrentUser();
        user.setNickname(request.nickname().trim());
        user.setAvatarUrl(normalize(request.avatarUrl()));
        user.setBio(normalize(request.bio()));
        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticatedUser authenticatedUser)) {
            throw new ResourceNotFoundException("当前登录用户不存在");
        }

        return userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("当前登录用户不存在"));
    }

    private MeProfileResponse toResponse(User user) {
        return new MeProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getRole().name()
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
