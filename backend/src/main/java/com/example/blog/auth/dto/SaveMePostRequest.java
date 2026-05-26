package com.example.blog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveMePostRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 255, message = "标题长度不能超过255个字符")
        String title,

        @Size(max = 128, message = "Slug长度不能超过128个字符")
        String slug,

        @Size(max = 512, message = "摘要长度不能超过512个字符")
        String summary,

        @NotBlank(message = "Markdown 内容不能为空")
        String contentMarkdown,

        @Size(max = 512, message = "封面图链接长度不能超过512个字符")
        String coverImageUrl,

        @NotBlank(message = "分类不能为空")
        String categorySlug,

        List<String> tagSlugs
) {
}
