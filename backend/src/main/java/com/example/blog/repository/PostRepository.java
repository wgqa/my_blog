package com.example.blog.repository;

import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import com.example.blog.model.PostVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByStatusAndVisibility(Pageable pageable, PostStatus status, PostVisibility visibility);

    @EntityGraph(attributePaths = {"author", "category", "postTags", "postTags.tag"})
    Optional<Post> findBySlugAndStatusAndVisibility(String slug, PostStatus status, PostVisibility visibility);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByCategorySlugAndStatusAndVisibility(String categorySlug, PostStatus status, PostVisibility visibility, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.visibility = :visibility AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.contentMarkdown) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> searchPublicPosts(@Param("keyword") String keyword, @Param("status") PostStatus status, @Param("visibility") PostVisibility visibility, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByAuthorUsernameAndStatusAndVisibility(String username, PostStatus status, PostVisibility visibility, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT p FROM Post p WHERE p.status <> 'DELETED' AND (p.author.id = :userId OR " +
           "EXISTS (SELECT 1 FROM Post p2 JOIN p2.allowedReaders r WHERE p2.id = p.id AND r.id = :userId)) " +
           "ORDER BY p.updatedAt DESC")
    Page<Post> findAccessiblePosts(@Param("userId") Long userId, Pageable pageable);
}
