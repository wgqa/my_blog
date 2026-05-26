package com.example.blog.admin;

import com.example.blog.admin.dto.AdminTagResponse;
import com.example.blog.admin.dto.SaveTagRequest;
import com.example.blog.exception.BadRequestException;
import com.example.blog.exception.ConflictException;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.Tag;
import com.example.blog.repository.PostTagRepository;
import com.example.blog.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AdminTagService {

    private static final Pattern NON_LATIN_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    public AdminTagService(TagRepository tagRepository, PostTagRepository postTagRepository) {
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminTagResponse> listTags() {
        return tagRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminTagResponse createTag(SaveTagRequest request) {
        String name = request.name().trim();
        if (tagRepository.existsByName(name)) {
            throw new ConflictException("标签名称已存在: " + name);
        }

        String slug = resolveSlug(request.slug(), name, null);
        LocalDateTime now = LocalDateTime.now();

        Tag tag = new Tag();
        tag.setName(name);
        tag.setSlug(slug);
        tag.setCreatedAt(now);
        tag.setUpdatedAt(now);
        return toResponse(tagRepository.save(tag));
    }

    @Transactional
    public AdminTagResponse updateTag(Long id, SaveTagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在: " + id));

        String name = request.name().trim();
        if (tagRepository.existsByNameAndIdNot(name, id)) {
            throw new ConflictException("标签名称已存在: " + name);
        }

        tag.setName(name);
        tag.setSlug(resolveSlug(request.slug(), name, id));
        tag.setUpdatedAt(LocalDateTime.now());
        return toResponse(tagRepository.save(tag));
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在: " + id));

        if (postTagRepository.existsByTagId(id)) {
            throw new BadRequestException("当前标签仍被文章引用，无法删除");
        }

        tagRepository.delete(tag);
    }

    private String resolveSlug(String requestedSlug, String fallbackName, Long currentTagId) {
        String slug = slugify(normalize(requestedSlug) == null ? fallbackName : requestedSlug);
        boolean exists = currentTagId == null
                ? tagRepository.existsBySlug(slug)
                : tagRepository.existsBySlugAndIdNot(slug, currentTagId);
        if (exists) {
            throw new ConflictException("标签 slug 已存在: " + slug);
        }
        return slug;
    }

    private AdminTagResponse toResponse(Tag tag) {
        return new AdminTagResponse(tag.getId(), tag.getName(), tag.getSlug());
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
        return slug.isEmpty() ? "tag" : slug;
    }
}
