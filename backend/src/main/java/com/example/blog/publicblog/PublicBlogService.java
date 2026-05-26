package com.example.blog.publicblog;

import com.example.blog.common.PageResponse;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.Category;
import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import com.example.blog.model.User;
import com.example.blog.model.UserRole;
import com.example.blog.model.UserStatus;
import com.example.blog.publicblog.dto.PublicAuthorProfile;
import com.example.blog.publicblog.dto.PublicCategoryItem;
import com.example.blog.publicblog.dto.PublicPostDetail;
import com.example.blog.publicblog.dto.PublicPostListItem;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicBlogService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public PublicBlogService(
            PostRepository postRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository
    ) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<PublicPostListItem> listPublishedPosts(int page, int size) {
        return PageResponse.from(postRepository.findByStatus(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt", "id")),
                PostStatus.PUBLISHED
        ).map(this::toItem));
    }

    public List<PublicCategoryItem> listCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(this::toCategoryItem)
                .toList();
    }

    public PageResponse<PublicPostListItem> listPublishedPostsByCategory(String categorySlug, int page, int size) {
        ensureCategoryExists(categorySlug);
        return PageResponse.from(postRepository.findByCategorySlugAndStatus(
                categorySlug,
                PostStatus.PUBLISHED,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt", "id"))
        ).map(this::toItem));
    }

    public PageResponse<PublicPostListItem> searchPublishedPosts(String keyword, int page, int size) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return listPublishedPosts(page, size);
        }

        return PageResponse.from(postRepository
                .findByStatusAndTitleContainingIgnoreCaseOrStatusAndSummaryContainingIgnoreCaseOrStatusAndContentMarkdownContainingIgnoreCase(
                        PostStatus.PUBLISHED,
                        normalizedKeyword,
                        PostStatus.PUBLISHED,
                        normalizedKeyword,
                        PostStatus.PUBLISHED,
                        normalizedKeyword,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt", "id"))
                )
                .map(this::toItem));
    }

    public PublicPostDetail getPublishedPostDetail(String slug) {
        Post post = postRepository.findBySlugAndStatus(slug, PostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("文章不存在: " + slug));
        return new PublicPostDetail(
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getContentHtml(),
                post.getAuthor().getNickname(),
                post.getAuthor().getUsername(),
                post.getPublishedAt(),
                post.getCategory().getName(),
                post.getPostTags().stream().map(postTag -> postTag.getTag().getName()).toList()
        );
    }

    public PublicAuthorProfile getAuthorProfile(String username, int page, int size) {
        User author = userRepository.findByUsernameAndRoleAndStatus(username, UserRole.AUTHOR, UserStatus.ENABLED)
                .orElseThrow(() -> new ResourceNotFoundException("作者不存在: " + username));

        PageResponse<PublicPostListItem> posts = PageResponse.from(postRepository.findByAuthorUsernameAndStatus(
                username,
                PostStatus.PUBLISHED,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt", "id"))
        ).map(this::toItem));

        return new PublicAuthorProfile(
                author.getUsername(),
                author.getNickname(),
                author.getAvatarUrl(),
                author.getBio(),
                posts
        );
    }

    private void ensureCategoryExists(String categorySlug) {
        categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在: " + categorySlug));
    }

    private PublicPostListItem toItem(Post post) {
        return new PublicPostListItem(
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getAuthor().getNickname(),
                post.getPublishedAt(),
                post.getCategory().getName()
        );
    }

    private PublicCategoryItem toCategoryItem(Category category) {
        return new PublicCategoryItem(category.getName(), category.getSlug(), category.getDescription());
    }
}
