package com.example.blog.auth.dto;

public record MeProfileResponse(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        String bio,
        String role
) {
}
