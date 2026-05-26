package com.example.blog.publicblog.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PublicPostDetail(
        String title,
        String slug,
        String summary,
        String contentHtml,
        String author,
        String authorUsername,
        LocalDateTime publishedAt,
        String category,
        List<String> tags
) {
}
