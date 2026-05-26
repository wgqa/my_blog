package com.example.blog.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminPostDetailResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String contentMarkdown,
        String contentHtml,
        String coverImageUrl,
        String authorUsername,
        String authorNickname,
        String categorySlug,
        List<String> tagSlugs,
        String status,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
