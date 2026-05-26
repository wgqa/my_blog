package com.example.blog.admin;

import com.example.blog.admin.dto.AdminPostDetailResponse;
import com.example.blog.admin.dto.AdminPostListItemResponse;
import com.example.blog.auth.dto.SaveMePostRequest;
import com.example.blog.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final AdminPostService adminPostService;

    public AdminPostController(AdminPostService adminPostService) {
        this.adminPostService = adminPostService;
    }

    @GetMapping
    public PageResponse<AdminPostListItemResponse> posts(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return adminPostService.listPosts(page, size);
    }

    @GetMapping("/{id}")
    public AdminPostDetailResponse post(@PathVariable Long id) {
        return adminPostService.getPost(id);
    }

    @PutMapping("/{id}")
    public AdminPostDetailResponse updatePost(@PathVariable Long id, @Valid @RequestBody SaveMePostRequest request) {
        return adminPostService.updatePost(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deletePost(@PathVariable Long id) {
        adminPostService.deletePost(id);
    }
}
