package com.example.blog.admin;

import com.example.blog.admin.dto.AdminCategoryResponse;
import com.example.blog.admin.dto.SaveCategoryRequest;
import com.example.blog.exception.BadRequestException;
import com.example.blog.exception.ConflictException;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.Category;
import com.example.blog.model.PostStatus;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AdminCategoryService {

    private static final Pattern NON_LATIN_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    public AdminCategoryService(CategoryRepository categoryRepository, PostRepository postRepository) {
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminCategoryResponse createCategory(SaveCategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException("分类名称已存在: " + name);
        }

        String slug = resolveSlug(request.slug(), name, null);
        LocalDateTime now = LocalDateTime.now();

        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(normalize(request.description()));
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public AdminCategoryResponse updateCategory(Long id, SaveCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在: " + id));

        String name = request.name().trim();
        if (categoryRepository.existsByNameAndIdNot(name, id)) {
            throw new ConflictException("分类名称已存在: " + name);
        }

        category.setName(name);
        category.setSlug(resolveSlug(request.slug(), name, id));
        category.setDescription(normalize(request.description()));
        category.setUpdatedAt(LocalDateTime.now());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在: " + id));

        if (postRepository.existsByCategoryIdAndStatusNot(id, PostStatus.DELETED)) {
            throw new BadRequestException("当前分类下仍有文章，无法删除");
        }

        categoryRepository.delete(category);
    }

    private String resolveSlug(String requestedSlug, String fallbackName, Long currentCategoryId) {
        String slug = slugify(normalize(requestedSlug) == null ? fallbackName : requestedSlug);
        boolean exists = currentCategoryId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, currentCategoryId);
        if (exists) {
            throw new ConflictException("分类 slug 已存在: " + slug);
        }
        return slug;
    }

    private AdminCategoryResponse toResponse(Category category) {
        return new AdminCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription()
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
        return slug.isEmpty() ? "category" : slug;
    }
}
