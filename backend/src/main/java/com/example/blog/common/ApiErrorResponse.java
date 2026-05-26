package com.example.blog.common;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}
