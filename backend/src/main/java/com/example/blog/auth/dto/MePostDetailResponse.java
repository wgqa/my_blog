package com.example.blog.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MePostDetailResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String contentMarkdown,
        String contentHtml,
        String coverImageUrl,
        String categorySlug,
        List<String> tagSlugs,
        String status,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
