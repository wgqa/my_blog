package com.example.blog.admin;

import com.example.blog.admin.dto.AdminCategoryResponse;
import com.example.blog.admin.dto.SaveCategoryRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping
    public List<AdminCategoryResponse> categories() {
        return adminCategoryService.listCategories();
    }

    @PostMapping
    public AdminCategoryResponse createCategory(@Valid @RequestBody SaveCategoryRequest request) {
        return adminCategoryService.createCategory(request);
    }

    @PutMapping("/{id}")
    public AdminCategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody SaveCategoryRequest request) {
        return adminCategoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
    }
}
