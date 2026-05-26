package com.example.blog.admin;

import com.example.blog.admin.dto.AdminTagResponse;
import com.example.blog.admin.dto.SaveTagRequest;
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
@RequestMapping("/api/admin/tags")
public class AdminTagController {

    private final AdminTagService adminTagService;

    public AdminTagController(AdminTagService adminTagService) {
        this.adminTagService = adminTagService;
    }

    @GetMapping
    public List<AdminTagResponse> tags() {
        return adminTagService.listTags();
    }

    @PostMapping
    public AdminTagResponse createTag(@Valid @RequestBody SaveTagRequest request) {
        return adminTagService.createTag(request);
    }

    @PutMapping("/{id}")
    public AdminTagResponse updateTag(@PathVariable Long id, @Valid @RequestBody SaveTagRequest request) {
        return adminTagService.updateTag(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        adminTagService.deleteTag(id);
    }
}
