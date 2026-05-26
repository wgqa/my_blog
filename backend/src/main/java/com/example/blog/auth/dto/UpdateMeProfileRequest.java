package com.example.blog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMeProfileRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 64, message = "昵称长度不能超过64个字符")
        String nickname,

        @Size(max = 512, message = "头像链接长度不能超过512个字符")
        String avatarUrl,

        @Size(max = 2000, message = "个人简介长度不能超过2000个字符")
        String bio
) {
}
