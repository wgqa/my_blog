package com.example.blog.admin;

import com.example.blog.admin.dto.AdminUserItemResponse;
import com.example.blog.admin.dto.AdminUserResponse;
import com.example.blog.admin.dto.CreateAuthorRequest;
import com.example.blog.admin.dto.UpdateUserStatusRequest;
import com.example.blog.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public PageResponse<AdminUserItemResponse> authors(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return adminUserService.listAuthors(page, size);
    }

    @PostMapping
    public AdminUserResponse createAuthor(@Valid @RequestBody CreateAuthorRequest request) {
        return adminUserService.createAuthor(request);
    }

    @PutMapping("/{id}/status")
    public AdminUserResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return adminUserService.updateUserStatus(id, request);
    }
}
