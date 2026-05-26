package com.example.blog.publicblog.dto;

import com.example.blog.common.PageResponse;

public record PublicAuthorProfile(
        String username,
        String nickname,
        String avatarUrl,
        String bio,
        PageResponse<PublicPostListItem> posts
) {
}
