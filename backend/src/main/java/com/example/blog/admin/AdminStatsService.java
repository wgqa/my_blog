package com.example.blog.admin;

import com.example.blog.admin.dto.AdminStatsResponse;
import com.example.blog.model.PostStatus;
import com.example.blog.model.UserRole;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminStatsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public AdminStatsService(
            UserRepository userRepository,
            PostRepository postRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                userRepository.countByRole(UserRole.AUTHOR),
                postRepository.countByStatus(PostStatus.PUBLISHED),
                categoryRepository.count(),
                tagRepository.count()
        );
    }
}
