package com.example.blog.githubimport;

import jakarta.validation.constraints.NotBlank;

public record GitHubImportRequest(
        @NotBlank String token,
        @NotBlank String repo,
        @NotBlank String path,
        String branch
) {
}
