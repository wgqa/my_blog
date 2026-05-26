package com.example.blog.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveCategoryRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 64, message = "分类名称长度不能超过64个字符")
        String name,

        @Size(max = 64, message = "分类 slug 长度不能超过64个字符")
        String slug,

        @Size(max = 255, message = "分类描述长度不能超过255个字符")
        String description
) {
}
