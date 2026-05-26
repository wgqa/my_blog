package com.example.blog.auth.dto;

public record AuthResponse(String token, AuthUserResponse user) {
}
