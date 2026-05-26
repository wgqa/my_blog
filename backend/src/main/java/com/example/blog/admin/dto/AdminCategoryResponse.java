package com.example.blog.admin.dto;

public record AdminCategoryResponse(
        Long id,
        String name,
        String slug,
        String description
) {
}
