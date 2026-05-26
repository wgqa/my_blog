package com.example.blog.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveTagRequest(
        @NotBlank(message = "标签名称不能为空")
        @Size(max = 64, message = "标签名称长度不能超过64个字符")
        String name,

        @Size(max = 64, message = "标签 slug 长度不能超过64个字符")
        String slug
) {
}
