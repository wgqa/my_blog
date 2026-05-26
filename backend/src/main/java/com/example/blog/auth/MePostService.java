package com.example.blog.auth;

import com.example.blog.auth.dto.MePostDetailResponse;
import com.example.blog.auth.dto.MePostListItemResponse;
import com.example.blog.auth.dto.SaveMePostRequest;
import com.example.blog.common.PageResponse;
import com.example.blog.common.MarkdownRenderService;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.Category;
import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import com.example.blog.model.PostTag;
import com.example.blog.model.PostTagId;
import com.example.blog.model.Tag;
import com.example.blog.model.User;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.TagRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class MePostService {

    private static final Pattern NON_LATIN_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    private final MeService meService;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final MarkdownRenderService markdownRenderService;

    public MePostService(
            MeService meService,
            PostRepository postRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            MarkdownRenderService markdownRenderService
    ) {
        this.meService = meService;
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.markdownRenderService = markdownRenderService;
    }

    @Transactional(readOnly = true)
    public PageResponse<MePostListItemResponse> listMyPosts(int page, int size) {
        User currentUser = meService.getCurrentUser();
        return PageResponse.from(postRepository.findByAuthorIdAndStatus(
                currentUser.getId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "id")),
                PostStatus.PUBLISHED
        ).map(this::toListItem));
    }

    @Transactional(readOnly = true)
    public MePostDetailResponse getMyPost(Long postId) {
        return toDetail(getOwnedPost(postId));
    }

    @Transactional
    public MePostDetailResponse createMyPost(SaveMePostRequest request) {
        User currentUser = meService.getCurrentUser();
        Post post = new Post();
        LocalDateTime now = LocalDateTime.now();
        post.setAuthor(currentUser);
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(now);
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        applyPostContent(post, request, null);
        return toDetail(postRepository.save(post));
    }

    @Transactional
    public MePostDetailResponse updateMyPost(Long postId, SaveMePostRequest request) {
        Post post = getOwnedPost(postId);
        applyPostContent(post, request, post.getId());
        post.setUpdatedAt(LocalDateTime.now());
        return toDetail(postRepository.save(post));
    }

    @Transactional
    public void deleteMyPost(Long postId) {
        Post post = getOwnedPost(postId);
        post.setStatus(PostStatus.DELETED);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    private Post getOwnedPost(Long postId) {
        User currentUser = meService.getCurrentUser();
        return postRepository.findByIdAndAuthorIdAndStatusNot(postId, currentUser.getId(), PostStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("文章不存在: " + postId));
    }

    private void applyPostContent(Post post, SaveMePostRequest request, Long currentPostId) {
        post.setTitle(request.title().trim());
        post.setSlug(resolveSlug(request, currentPostId));
        post.setSummary(normalize(request.summary()));
        String markdown = request.contentMarkdown().trim();
        post.setContentMarkdown(markdown);
        post.setContentHtml(markdownRenderService.render(markdown));
        post.setCoverImageUrl(normalize(request.coverImageUrl()));
        post.setCategory(resolveCategory(request.categorySlug()));
        syncTags(post, request.tagSlugs());
    }

    private String resolveSlug(SaveMePostRequest request, Long currentPostId) {
        String preferred = normalize(request.slug());
        String base = slugify(preferred == null ? request.title() : preferred);
        String candidate = base;
        int suffix = 2;

        while (slugExists(candidate, currentPostId)) {
            candidate = base + "-" + suffix;
            suffix++;
        }

        return candidate;
    }

    private boolean slugExists(String slug, Long currentPostId) {
        if (currentPostId == null) {
            return postRepository.existsBySlugAndStatusNot(slug, PostStatus.DELETED);
        }
        return postRepository.existsBySlugAndIdNotAndStatusNot(slug, currentPostId, PostStatus.DELETED);
    }

    private Category resolveCategory(String categorySlug) {
        return categoryRepository.findBySlug(categorySlug.trim())
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在: " + categorySlug));
    }

    private void syncTags(Post post, List<String> tagSlugs) {
        Map<Long, PostTag> existingByTagId = post.getPostTags().stream()
                .collect(LinkedHashMap::new, (map, postTag) -> map.put(postTag.getTag().getId(), postTag), Map::putAll);
        Set<PostTag> nextTags = resolveTags(post, tagSlugs, existingByTagId);
        post.getPostTags().retainAll(nextTags);
        for (PostTag postTag : nextTags) {
            if (!post.getPostTags().contains(postTag)) {
                post.getPostTags().add(postTag);
            }
        }
    }

    private Set<PostTag> resolveTags(Post post, List<String> tagSlugs, Map<Long, PostTag> existingByTagId) {
        Set<PostTag> result = new LinkedHashSet<>();
        if (tagSlugs == null) {
            return result;
        }

        for (String slug : tagSlugs.stream().filter(value -> value != null && !value.isBlank()).map(String::trim).distinct().toList()) {
            Tag tag = tagRepository.findBySlug(slug)
                    .orElseThrow(() -> new ResourceNotFoundException("标签不存在: " + slug));
            PostTag postTag = existingByTagId.get(tag.getId());
            if (postTag == null) {
                postTag = new PostTag();
                postTag.setId(new PostTagId());
                postTag.setPost(post);
                postTag.setTag(tag);
            }
            result.add(postTag);
        }
        return result;
    }

    private MePostListItemResponse toListItem(Post post) {
        return new MePostListItemResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getCategory().getName(),
                post.getStatus().name(),
                post.getPublishedAt(),
                post.getUpdatedAt()
        );
    }

    private MePostDetailResponse toDetail(Post post) {
        return new MePostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getContentMarkdown(),
                post.getContentHtml(),
                post.getCoverImageUrl(),
                post.getCategory().getSlug(),
                post.getPostTags().stream().map(postTag -> postTag.getTag().getSlug()).toList(),
                post.getStatus().name(),
                post.getPublishedAt(),
                post.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        String slug = NON_LATIN_ALPHANUMERIC.matcher(normalized).replaceAll("-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
        return slug.isEmpty() ? "post" : slug;
    }
}
