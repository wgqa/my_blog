package com.example.blog.publicblog.dto;

import java.time.LocalDateTime;

public record PublicPostListItem(
        String title,
        String slug,
        String summary,
        String author,
        LocalDateTime publishedAt,
        String category
) {
}
