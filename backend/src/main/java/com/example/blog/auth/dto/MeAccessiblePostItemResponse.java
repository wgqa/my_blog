package com.example.blog.auth.dto;

import java.time.LocalDateTime;

public record MeAccessiblePostItemResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String authorUsername,
        String authorNickname,
        String categoryName,
        String visibility,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
