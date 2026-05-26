package com.example.blog.auth.dto;

public record MeUploadResponse(
        Long id,
        String fileName,
        String originalName,
        String contentType,
        Long fileSize,
        String url
) {
}
