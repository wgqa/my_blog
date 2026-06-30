package com.example.blog.auth;

import com.example.blog.auth.dto.MeAccessiblePostDetailResponse;
import com.example.blog.auth.dto.MeAccessiblePostItemResponse;
import com.example.blog.auth.dto.MeProfileResponse;
import com.example.blog.auth.dto.UpdateMeProfileRequest;
import com.example.blog.common.PageResponse;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import com.example.blog.model.User;
import com.example.blog.model.UserStatus;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.security.AuthenticatedUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MeService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public MeService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public MeProfileResponse getProfile() {
        User user = getCurrentUser();
        return toResponse(user);
    }

    @Transactional
    public MeProfileResponse updateProfile(UpdateMeProfileRequest request) {
        User user = getCurrentUser();
        user.setNickname(request.nickname().trim());
        user.setAvatarUrl(normalize(request.avatarUrl()));
        user.setBio(normalize(request.bio()));
        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticatedUser authenticatedUser)) {
            throw new ResourceNotFoundException("当前登录用户不存在");
        }

        return userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("当前登录用户不存在"));
    }

    public record UserSearchResult(Long id, String username, String nickname, String avatarUrl) {}

    @Transactional(readOnly = true)
    public List<UserSearchResult> searchUsers(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        return userRepository
                .findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCaseAndStatus(
                        normalized, normalized, UserStatus.ENABLED,
                        PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "username"))
                )
                .stream()
                .map(u -> new UserSearchResult(u.getId(), u.getUsername(), u.getNickname(), u.getAvatarUrl()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<MeAccessiblePostItemResponse> listAccessiblePosts(int page, int size) {
        User currentUser = getCurrentUser();
        return PageResponse.from(
                postRepository.findAccessiblePosts(
                        currentUser.getId(),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "id"))
                ).map(this::toAccessibleItem)
        );
    }

    @Transactional(readOnly = true)
    public MeAccessiblePostDetailResponse getAccessiblePostDetail(Long id) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findByIdAndStatusNot(id, PostStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("文章不存在: " + id));

        boolean isPublic = post.getVisibility() == com.example.blog.model.PostVisibility.PUBLIC;
        boolean isAuthor = post.getAuthor().getId().equals(currentUser.getId());
        boolean isAllowedReader = post.getAllowedReaders().stream()
                .anyMatch(r -> r.getId().equals(currentUser.getId()));
        if (!isPublic && !isAuthor && !isAllowedReader) {
            throw new ResourceNotFoundException("文章不存在: " + id);
        }

        return new MeAccessiblePostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getContentHtml(),
                post.getAuthor().getUsername(),
                post.getAuthor().getNickname(),
                post.getCategory().getName(),
                post.getPostTags().stream().map(pt -> pt.getTag().getName()).toList(),
                post.getVisibility().name(),
                post.getPublishedAt(),
                post.getUpdatedAt()
        );
    }

    private MeProfileResponse toResponse(User user) {
        return new MeProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getRole().name()
        );
    }

    private MeAccessiblePostItemResponse toAccessibleItem(Post post) {
        return new MeAccessiblePostItemResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getAuthor().getUsername(),
                post.getAuthor().getNickname(),
                post.getCategory().getName(),
                post.getVisibility().name(),
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
}
