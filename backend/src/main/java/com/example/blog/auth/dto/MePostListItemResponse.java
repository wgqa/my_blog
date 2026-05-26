package com.example.blog.auth.dto;

import java.time.LocalDateTime;

public record MePostListItemResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String categoryName,
        String status,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
