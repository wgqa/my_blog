package com.example.blog;

import com.example.blog.model.Category;
import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import com.example.blog.model.Tag;
import com.example.blog.model.User;
import com.example.blog.model.UserRole;
import com.example.blog.model.UserStatus;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BlogApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User author;
    private User otherAuthor;
    private Category javaCategory;
    private Tag springTag;
    private Post authorPost;
    private Post otherAuthorPost;
    private String adminToken;
    private String authorToken;

    @BeforeEach
    void setUp() throws Exception {
        postRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        admin = saveUser("admin", "admin123456", "管理员", UserRole.ADMIN, UserStatus.ENABLED);
        author = saveUser("alice", "password123", "Alice", UserRole.AUTHOR, UserStatus.ENABLED);
        otherAuthor = saveUser("bob", "password123", "Bob", UserRole.AUTHOR, UserStatus.ENABLED);

        javaCategory = new Category();
        javaCategory.setName("Java");
        javaCategory.setSlug("java");
        javaCategory.setDescription("Java 分类");
        javaCategory.setCreatedAt(LocalDateTime.now());
        javaCategory.setUpdatedAt(LocalDateTime.now());
        javaCategory = categoryRepository.save(javaCategory);

        springTag = new Tag();
        springTag.setName("Spring");
        springTag.setSlug("spring");
        springTag.setCreatedAt(LocalDateTime.now());
        springTag.setUpdatedAt(LocalDateTime.now());
        springTag = tagRepository.save(springTag);

        authorPost = savePost(author, javaCategory, "作者文章", "author-post", PostStatus.PUBLISHED, "作者摘要", "# 作者正文");
        otherAuthorPost = savePost(otherAuthor, javaCategory, "他人文章", "other-post", PostStatus.PUBLISHED, "他人摘要", "# 他人正文");
        savePost(author, javaCategory, "已删除文章", "deleted-post", PostStatus.DELETED, "已删除摘要", "# 已删除正文");

        adminToken = login("admin", "admin123456");
        authorToken = login("alice", "password123");
    }

    @Test
    void loginReturnsTokenAndUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "alice",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.username").value("alice"))
                .andExpect(jsonPath("$.user.role").value("AUTHOR"));
    }

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/me/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointsRejectAuthorRole() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", bearer(authorToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicPostsOnlyReturnPublishedContent() throws Exception {
        mockMvc.perform(get("/api/public/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].slug").exists());

        mockMvc.perform(get("/api/public/posts/deleted-post"))
                .andExpect(status().isNotFound());
    }

    @Test
    void authorCanListCreateUpdateAndDeleteOwnPosts() throws Exception {
        mockMvc.perform(get("/api/me/posts")
                        .header("Authorization", bearer(authorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].slug").value("author-post"));

        String createdBody = mockMvc.perform(post("/api/me/posts")
                        .header("Authorization", bearer(authorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "新的作者文章",
                                "slug", "",
                                "summary", "新的摘要",
                                "contentMarkdown", "# 新正文",
                                "coverImageUrl", "",
                                "categorySlug", "java",
                                "tagSlugs", new String[]{"spring"}
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("post"))
                .andExpect(jsonPath("$.contentHtml").value(org.hamcrest.Matchers.containsString("<h1>新正文</h1>")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(createdBody).get("id").asLong();

        mockMvc.perform(put("/api/me/posts/{id}", createdId)
                        .header("Authorization", bearer(authorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "更新后的文章",
                                "slug", "updated-post",
                                "summary", "更新摘要",
                                "contentMarkdown", "# 更新正文",
                                "coverImageUrl", "http://example.com/cover.png",
                                "categorySlug", "java",
                                "tagSlugs", new String[]{"spring"}
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("updated-post"))
                .andExpect(jsonPath("$.coverImageUrl").value("http://example.com/cover.png"));

        mockMvc.perform(delete("/api/me/posts/{id}", createdId)
                        .header("Authorization", bearer(authorToken)))
                .andExpect(status().isNoContent());

        assertThat(postRepository.findById(createdId)).isPresent();
        assertThat(postRepository.findById(createdId).orElseThrow().getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    void authorCannotAccessOtherAuthorsPost() throws Exception {
        mockMvc.perform(get("/api/me/posts/{id}", otherAuthorPost.getId())
                        .header("Authorization", bearer(authorToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/me/posts/{id}", otherAuthorPost.getId())
                        .header("Authorization", bearer(authorToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanManageUsersAndReadStats() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "charlie",
                                "password", "password123",
                                "nickname", "Charlie",
                                "email", "charlie@example.com"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("charlie"))
                .andExpect(jsonPath("$.role").value("AUTHOR"))
                .andExpect(jsonPath("$.status").value("ENABLED"));

        User createdAuthor = userRepository.findByUsername("charlie").orElseThrow();

        mockMvc.perform(put("/api/admin/users/{id}/status", createdAuthor.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISABLED"));

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(4))
                .andExpect(jsonPath("$.totalAuthors").value(3))
                .andExpect(jsonPath("$.totalPublishedPosts").value(2))
                .andExpect(jsonPath("$.totalCategories").value(1))
                .andExpect(jsonPath("$.totalTags").value(1));
    }

    @Test
    void adminCanUpdateAndDeletePosts() throws Exception {
        mockMvc.perform(put("/api/admin/posts/{id}", authorPost.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "管理员改文",
                                "slug", "admin-updated-post",
                                "summary", "管理员摘要",
                                "contentMarkdown", "# 管理员正文",
                                "coverImageUrl", "",
                                "categorySlug", "java",
                                "tagSlugs", new String[]{"spring"}
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("admin-updated-post"));

        mockMvc.perform(delete("/api/admin/posts/{id}", authorPost.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        assertThat(postRepository.findById(authorPost.getId()).orElseThrow().getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    void adminCanManageCategoriesAndTags() throws Exception {
        String categoryBody = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Spring Boot",
                                "slug", "spring-boot",
                                "description", "Spring Boot 分类"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("spring-boot"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long categoryId = objectMapper.readTree(categoryBody).get("id").asLong();

        mockMvc.perform(put("/api/admin/categories/{id}", categoryId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Spring MVC",
                                "slug", "spring-mvc",
                                "description", "Spring MVC 分类"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("spring-mvc"));

        mockMvc.perform(delete("/api/admin/categories/{id}", categoryId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        String tagBody = mockMvc.perform(post("/api/admin/tags")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Vue",
                                "slug", "vue"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("vue"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long tagId = objectMapper.readTree(tagBody).get("id").asLong();

        mockMvc.perform(put("/api/admin/tags/{id}", tagId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Vue3",
                                "slug", "vue3"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("vue3"));

        mockMvc.perform(delete("/api/admin/tags/{id}", tagId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    private User saveUser(String username, String password, String nickname, UserRole role, UserStatus status) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Post savePost(User owner, Category category, String title, String slug, PostStatus status, String summary, String markdown) {
        Post post = new Post();
        post.setTitle(title);
        post.setSlug(slug);
        post.setSummary(summary);
        post.setContentMarkdown(markdown);
        post.setContentHtml("<h1>" + markdown.substring(2) + "</h1>");
        post.setAuthor(owner);
        post.setCategory(category);
        post.setStatus(status);
        LocalDateTime now = LocalDateTime.now();
        post.setPublishedAt(status == PostStatus.PUBLISHED ? now : null);
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        return postRepository.save(post);
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
