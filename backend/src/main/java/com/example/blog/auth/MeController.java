package com.example.blog.auth;

import com.example.blog.auth.dto.MePostDetailResponse;
import com.example.blog.auth.dto.MePostListItemResponse;
import com.example.blog.auth.dto.MeProfileResponse;
import com.example.blog.auth.dto.MeUploadResponse;
import com.example.blog.auth.dto.SaveMePostRequest;
import com.example.blog.auth.dto.UpdateMeProfileRequest;
import com.example.blog.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final MeService meService;
    private final MePostService mePostService;
    private final MeUploadService meUploadService;

    public MeController(MeService meService, MePostService mePostService, MeUploadService meUploadService) {
        this.meService = meService;
        this.mePostService = mePostService;
        this.meUploadService = meUploadService;
    }

    @GetMapping("/profile")
    public MeProfileResponse profile() {
        return meService.getProfile();
    }

    @PutMapping("/profile")
    public MeProfileResponse updateProfile(@Valid @RequestBody UpdateMeProfileRequest request) {
        return meService.updateProfile(request);
    }

    @GetMapping("/posts")
    public PageResponse<MePostListItemResponse> posts(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return mePostService.listMyPosts(page, size);
    }

    @GetMapping("/posts/{id}")
    public MePostDetailResponse post(@PathVariable Long id) {
        return mePostService.getMyPost(id);
    }

    @PostMapping("/posts")
    public MePostDetailResponse createPost(@Valid @RequestBody SaveMePostRequest request) {
        return mePostService.createMyPost(request);
    }

    @PutMapping("/posts/{id}")
    public MePostDetailResponse updatePost(@PathVariable Long id, @Valid @RequestBody SaveMePostRequest request) {
        return mePostService.updateMyPost(id, request);
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deletePost(@PathVariable Long id) {
        mePostService.deleteMyPost(id);
    }

    @PostMapping("/uploads")
    public MeUploadResponse upload(@RequestParam("file") MultipartFile file) {
        return meUploadService.uploadImage(file);
    }
}
