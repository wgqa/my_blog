package com.example.blog.admin.dto;

import java.time.LocalDateTime;

public record AdminPostListItemResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String authorUsername,
        String authorNickname,
        String categoryName,
        String status,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
