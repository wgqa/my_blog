package com.example.blog.admin.dto;

public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String role,
        String status
) {
}
