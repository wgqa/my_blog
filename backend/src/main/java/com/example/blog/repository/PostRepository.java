package com.example.blog.repository;

import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author", "category", "postTags", "postTags.tag"})
    Optional<Post> findBySlugAndStatus(String slug, PostStatus status);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByStatus(Pageable pageable, PostStatus status);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByAuthorIdAndStatus(Long authorId, Pageable pageable, PostStatus status);

    @EntityGraph(attributePaths = {"author", "category", "postTags", "postTags.tag"})
    Optional<Post> findByIdAndAuthorIdAndStatusNot(Long id, Long authorId, PostStatus status);

    @EntityGraph(attributePaths = {"author", "category", "postTags", "postTags.tag"})
    Optional<Post> findByIdAndStatusNot(Long id, PostStatus status);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByStatusNot(Pageable pageable, PostStatus status);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByCategorySlugAndStatus(String categorySlug, PostStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByStatusAndTitleContainingIgnoreCaseOrStatusAndSummaryContainingIgnoreCaseOrStatusAndContentMarkdownContainingIgnoreCase(
            PostStatus titleStatus,
            String titleKeyword,
            PostStatus summaryStatus,
            String summaryKeyword,
            PostStatus contentStatus,
            String contentKeyword,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByAuthorUsernameAndStatus(String username, PostStatus status, Pageable pageable);

    boolean existsBySlugAndStatusNot(String slug, PostStatus status);

    boolean existsBySlugAndIdNotAndStatusNot(String slug, Long id, PostStatus status);

    boolean existsByCategoryIdAndStatusNot(Long categoryId, PostStatus status);

    long countByStatus(PostStatus status);
}
