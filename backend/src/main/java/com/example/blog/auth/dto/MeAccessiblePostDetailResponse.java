package com.example.blog.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MeAccessiblePostDetailResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String contentHtml,
        String authorUsername,
        String authorNickname,
        String categoryName,
        List<String> tagNames,
        String visibility,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
