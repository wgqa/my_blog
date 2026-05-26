package com.example.blog.publicblog;

import com.example.blog.common.PageResponse;
import com.example.blog.publicblog.dto.PublicAuthorProfile;
import com.example.blog.publicblog.dto.PublicCategoryItem;
import com.example.blog.publicblog.dto.PublicPostDetail;
import com.example.blog.publicblog.dto.PublicPostListItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public Blog")
public class PublicBlogController {

    private final PublicBlogService publicBlogService;

    public PublicBlogController(PublicBlogService publicBlogService) {
        this.publicBlogService = publicBlogService;
    }

    @GetMapping("/posts")
    @Operation(summary = "分页获取公开文章列表")
    public PageResponse<PublicPostListItem> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return publicBlogService.listPublishedPosts(page, size);
    }

    @GetMapping("/posts/{slug}")
    @Operation(summary = "获取公开文章详情")
    public PublicPostDetail getPostDetail(@PathVariable String slug) {
        return publicBlogService.getPublishedPostDetail(slug);
    }

    @GetMapping("/authors/{username}")
    @Operation(summary = "获取作者主页信息")
    public PublicAuthorProfile getAuthorProfile(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return publicBlogService.getAuthorProfile(username, page, size);
    }

    @GetMapping("/categories")
    @Operation(summary = "获取公开分类列表")
    public List<PublicCategoryItem> listCategories() {
        return publicBlogService.listCategories();
    }

    @GetMapping("/categories/{slug}/posts")
    @Operation(summary = "分页获取某分类下的公开文章")
    public PageResponse<PublicPostListItem> listPostsByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return publicBlogService.listPublishedPostsByCategory(slug, page, size);
    }

    @GetMapping("/search")
    @Operation(summary = "按关键词搜索公开文章")
    public PageResponse<PublicPostListItem> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return publicBlogService.searchPublishedPosts(keyword, page, size);
    }
}
