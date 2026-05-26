package com.example.blog.common;

import java.util.List;

public record ValidationErrorResponse(
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> fieldErrors
) {
    public record FieldValidationError(String field, String message) {
    }
}
