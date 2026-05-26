package com.example.blog.admin.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalAuthors,
        long totalPublishedPosts,
        long totalCategories,
        long totalTags
) {
}
