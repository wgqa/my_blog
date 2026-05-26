package com.example.blog.admin.dto;

import java.time.LocalDateTime;

public record AdminUserItemResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
