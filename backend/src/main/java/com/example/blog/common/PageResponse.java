package com.example.blog.common;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        long total,
        int page,
        int size,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
    }
}
