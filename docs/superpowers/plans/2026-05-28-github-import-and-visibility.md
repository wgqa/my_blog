# GitHub 导入与文章可见性 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为现有博客系统增加两个功能：1) 文章可见性控制（PUBLIC/PRIVATE/RESTRICTED）；2) 通过 GitHub token 从指定仓库拉取 Markdown 文件自动发布为文章。

**Architecture:** 后端新增 PostVisibility 枚举与 ManyToMany 读者关联表，通过 Flyway 迁移加字段和表；公开 API 增加 visibility=PUBLIC 过滤，作者 API 支持可见性设置和读者管理。GitHub 导入独立为新的 service/controller 包，通过 GitHub API 获取文件内容并解析 frontmatter 后复用现有文章创建流程。

**Tech Stack:** Spring Boot 3, Spring Data JPA, Flyway, Vue 3, TypeScript, Pinia, Vue Router

---

## File Structure Map

### 现有文件改动
- Modify: `backend/src/main/java/com/example/blog/model/Post.java` — 加 visibility、allowedReaders
- Modify: `backend/src/main/java/com/example/blog/auth/dto/SaveMePostRequest.java` — 加 visibility、allowedReaderUsernames
- Modify: `backend/src/main/java/com/example/blog/auth/dto/MePostDetailResponse.java` — 加 visibility、allowedReaderUsernames
- Modify: `backend/src/main/java/com/example/blog/admin/dto/AdminPostDetailResponse.java` — 加 visibility、allowedReaderUsernames
- Modify: `backend/src/main/java/com/example/blog/auth/MePostService.java` — 处理 visibility 和 readers
- Modify: `backend/src/main/java/com/example/blog/admin/AdminPostService.java` — 处理 visibility 和 readers
- Modify: `backend/src/main/java/com/example/blog/repository/PostRepository.java` — 加 visibility 过滤查询
- Modify: `backend/src/main/java/com/example/blog/publicblog/PublicBlogService.java` — 只返回 PUBLIC
- Modify: `backend/src/main/java/com/example/blog/auth/MeController.java` — 加 accessible-posts 和 user-search
- Modify: `backend/src/main/java/com/example/blog/auth/MeService.java` — 加 accessible-posts 逻辑、用户搜索
- Modify: `backend/src/main/java/com/example/blog/repository/UserRepository.java` — 加用户搜索
- Modify: `frontend/src/types/me.ts` — 加 visibility 类型
- Modify: `frontend/src/api/me.ts` — 加用户搜索、accessible-posts、GitHub 导入 API
- Modify: `frontend/src/views/AuthorPostFormView.vue` — 加可见性选择器、用户搜索下拉
- Modify: `frontend/src/router/index.ts` — 加新路由
- Modify: `README.md` — 功能说明

### 新建文件
- New: `backend/src/main/java/com/example/blog/model/PostVisibility.java` — 可见性枚举
- New: `backend/src/main/java/com/example/blog/auth/dto/MeAccessiblePostItemResponse.java` — accessible-post 列表 DTO
- New: `backend/src/main/java/com/example/blog/auth/dto/MeAccessiblePostDetailResponse.java` — accessible-post 详情 DTO
- New: `backend/src/main/resources/db/migration/V2__add_post_visibility.sql` — 可见性迁移
- New: `backend/src/main/java/com/example/blog/githubimport/GitHubImportService.java` — 导入服务
- New: `backend/src/main/java/com/example/blog/githubimport/GitHubImportController.java` — 导入控制器
- New: `backend/src/main/java/com/example/blog/githubimport/GitHubImportRequest.java` — 导入请求 DTO
- New: `backend/src/main/java/com/example/blog/githubimport/GitHubImportRecord.java` — 导入记录实体
- New: `backend/src/main/resources/db/migration/V3__add_github_imports.sql` — 导入记录表
- New: `frontend/src/views/AuthorAccessiblePostsView.vue` — 可见文章列表页
- New: `frontend/src/views/AuthorGitHubImportView.vue` — GitHub 导入页
- New: `frontend/src/components/markdown/UserSearchDropdown.vue` — 用户搜索下拉组件

---

## Task 1: 数据库迁移 — 可见性字段与读者关联表

**Files:**
- New: `backend/src/main/resources/db/migration/V2__add_post_visibility.sql`
- New: `backend/src/main/java/com/example/blog/model/PostVisibility.java`

- [ ] **Step 1: 编写 Flyway 迁移 V2__add_post_visibility.sql**

```sql
ALTER TABLE posts ADD COLUMN visibility VARCHAR(16) NOT NULL DEFAULT 'PUBLIC';

CREATE TABLE post_readers (
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, user_id)
);
```

- [ ] **Step 2: 创建 PostVisibility 枚举**

```java
package com.example.blog.model;

public enum PostVisibility {
    PUBLIC, PRIVATE, RESTRICTED
}
```

## Task 2: Post 实体扩展

**Files:**
- Modify: `backend/src/main/java/com/example/blog/model/Post.java`

- [ ] **Step 1: 为 Post 实体添加 visibility 字段和 allowedReaders**

在 Post.java 中添加：

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 16)
private PostVisibility visibility = PostVisibility.PUBLIC;

@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "post_readers",
    joinColumns = @JoinColumn(name = "post_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
)
private Set<User> allowedReaders = new HashSet<>();
```

## Task 3: DTO 扩展

**Files:**
- Modify: `backend/src/main/java/com/example/blog/auth/dto/SaveMePostRequest.java`
- Modify: `backend/src/main/java/com/example/blog/auth/dto/MePostDetailResponse.java`
- Modify: `backend/src/main/java/com/example/blog/admin/dto/AdminPostDetailResponse.java`
- New: `backend/src/main/java/com/example/blog/auth/dto/MeAccessiblePostItemResponse.java`
- New: `backend/src/main/java/com/example/blog/auth/dto/MeAccessiblePostDetailResponse.java`

- [ ] **Step 1: SaveMePostRequest 加 visibility 和 allowedReaderUsernames**

```java
public record SaveMePostRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 255, message = "标题长度不能超过255个字符")
        String title,

        @Size(max = 128, message = "Slug长度不能超过128个字符")
        String slug,

        @Size(max = 512, message = "摘要长度不能超过512个字符")
        String summary,

        @NotBlank(message = "Markdown 内容不能为空")
        String contentMarkdown,

        @Size(max = 512, message = "封面图链接长度不能超过512个字符")
        String coverImageUrl,

        @NotBlank(message = "分类不能为空")
        String categorySlug,

        List<String> tagSlugs,

        String visibility,

        List<String> allowedReaderUsernames
) {}
```

- [ ] **Step 2: MePostDetailResponse 加 visibility 和 allowedReaderUsernames**

```java
public record MePostDetailResponse(
        Long id, String title, String slug, String summary,
        String contentMarkdown, String contentHtml,
        String coverImageUrl, String categorySlug,
        List<String> tagSlugs,
        String status, LocalDateTime publishedAt, LocalDateTime updatedAt,
        String visibility,
        List<String> allowedReaderUsernames
) {}
```

- [ ] **Step 3: AdminPostDetailResponse 加 visibility 和 allowedReaderUsernames**

```java
public record AdminPostDetailResponse(
        Long id, String title, String slug, String summary,
        String contentMarkdown, String contentHtml,
        String coverImageUrl,
        String authorUsername, String authorNickname,
        String categorySlug, List<String> tagSlugs,
        String status, LocalDateTime publishedAt, LocalDateTime updatedAt,
        String visibility,
        List<String> allowedReaderUsernames
) {}
```

- [ ] **Step 4: 创建 MeAccessiblePostItemResponse**

```java
package com.example.blog.auth.dto;

import java.time.LocalDateTime;

public record MeAccessiblePostItemResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String authorUsername,
        String authorNickname,
        String categoryName,
        String visibility,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {}
```

- [ ] **Step 5: 创建 MeAccessiblePostDetailResponse**

```java
package com.example.blog.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MeAccessiblePostDetailResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String contentHtml,
        String authorUsername,
        String authorNickname,
        String categoryName,
        List<String> tagNames,
        String visibility,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {}
```

## Task 4: 服务层 — 可见性处理

**Files:**
- Modify: `backend/src/main/java/com/example/blog/auth/MePostService.java`
- Modify: `backend/src/main/java/com/example/blog/admin/AdminPostService.java`

- [ ] **Step 1: MePostService 处理 visibility 和 readers**

在 `applyPostContent` 尾部添加：

```java
String vis = normalize(request.visibility());
post.setVisibility(vis == null ? PostVisibility.PUBLIC : PostVisibility.valueOf(vis.toUpperCase()));

syncAllowedReaders(post, request.allowedReaderUsernames());
```

新增 `syncAllowedReaders` 方法：

```java
private void syncAllowedReaders(Post post, List<String> usernames) {
    if (usernames == null || usernames.isEmpty()) {
        post.getAllowedReaders().clear();
        return;
    }
    Set<User> readers = usernames.stream()
            .map(u -> userRepository.findByUsername(u.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + u)))
            .collect(Collectors.toSet());
    post.getAllowedReaders().clear();
    post.getAllowedReaders().addAll(readers);
}
```

更新 `toDetail` 方法添加 visibility 和 allowedReaderUsernames。

需要在 MePostService 中注入 UserRepository。

- [ ] **Step 2: AdminPostService 做同样的修改**

与 MePostService 相同模式，在 `applyPostContent` 中添加 visibility 和 readers 处理，更新 `toDetail`。AdminPostService 不需要额外注入 UserRepository（已有 categoryRepository 等，新增即可）。

## Task 5: 公开 API 过滤 — 只返回 PUBLIC

**Files:**
- Modify: `backend/src/main/java/com/example/blog/repository/PostRepository.java`
- Modify: `backend/src/main/java/com/example/blog/publicblog/PublicBlogService.java`

- [ ] **Step 1: PostRepository 添加 visibility 过滤查询方法**

保留原有查询不破坏现有代码，新增带 visibility 参数的方法：

```java
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
```

- [ ] **Step 2: PublicBlogService 将所有查询改为 `...AndVisibility` 并传 `PostVisibility.PUBLIC`**

```java
// 原: postRepository.findByStatus(PageRequest.of(...), PostStatus.PUBLISHED)
// 改: postRepository.findByStatusAndVisibility(PageRequest.of(...), PostStatus.PUBLISHED, PostVisibility.PUBLIC)
```

同理改造所有公开接口查询方法（list、detail、category、search、author）。`getPublishedPostDetail` 在取不到时返回 404 而非区分"不存在"和"非公开"。

## Task 6: 用户搜索 + 可访问文章端点

**Files:**
- Modify: `backend/src/main/java/com/example/blog/auth/MeService.java`
- Modify: `backend/src/main/java/com/example/blog/auth/MeController.java`
- Modify: `backend/src/main/java/com/example/blog/repository/UserRepository.java`
- Modify: `backend/src/main/java/com/example/blog/repository/PostRepository.java`

- [ ] **Step 1: UserRepository 添加用户搜索**

```java
@EntityGraph(attributePaths = {})
Page<User> findByUsernameContainingIgnoreCaseAndStatus(String username, UserStatus status, Pageable pageable);

@EntityGraph(attributePaths = {})
Page<User> findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCaseAndStatus(String username, String nickname, UserStatus status, Pageable pageable);
```

- [ ] **Step 2: PostRepository 添加可访问文章查询**

```java
@EntityGraph(attributePaths = {"author", "category"})
@Query("SELECT p FROM Post p WHERE p.status <> 'DELETED' AND (p.author.id = :userId OR " +
       "EXISTS (SELECT 1 FROM Post p2 JOIN p2.allowedReaders r WHERE p2.id = p.id AND r.id = :userId)) " +
       "ORDER BY p.updatedAt DESC")
Page<Post> findAccessiblePosts(@Param("userId") Long userId, Pageable pageable);
```

- [ ] **Step 3: MeService 添加方法和 DTO**

新增方法：
- `searchUsers(String keyword)` — 搜索启用的用户，排除当前用户自己
- `listAccessiblePosts(int page, int size)` — 当前用户可读的非公开文章
- `getAccessiblePostDetail(Long id)` — 校验权限后返回详情

新建 UserSearchResult 内部 record。

- [ ] **Step 4: MeController 添加新端点**

```java
@GetMapping("/users/search")
public List<UserSearchResult> searchUsers(@RequestParam String q) {
    return meService.searchUsers(q);
}

@GetMapping("/accessible-posts")
public PageResponse<MeAccessiblePostItemResponse> accessiblePosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return meService.listAccessiblePosts(page, size);
}

@GetMapping("/accessible-posts/{id}")
public MeAccessiblePostDetailResponse accessiblePost(@PathVariable Long id) {
    return meService.getAccessiblePostDetail(id);
}
```

## Task 7: 前端类型和 API 扩展

**Files:**
- Modify: `frontend/src/types/me.ts`
- Modify: `frontend/src/api/me.ts`

- [ ] **Step 1: 更新 types/me.ts**

```typescript
export type PostVisibility = 'PUBLIC' | 'PRIVATE' | 'RESTRICTED'

export interface SaveMePostRequest {
  title: string
  slug: string | null
  summary: string | null
  contentMarkdown: string
  coverImageUrl: string | null
  categorySlug: string
  tagSlugs: string[]
  visibility: PostVisibility
  allowedReaderUsernames: string[]
}

export interface MePostDetail {
  // 现有字段...
  visibility: PostVisibility
  allowedReaderUsernames: string[]
}
```

定义新类型：

```typescript
export interface MeAccessiblePostItem {
  id: number
  title: string
  slug: string
  summary: string | null
  authorUsername: string
  authorNickname: string
  categoryName: string
  visibility: PostVisibility
  publishedAt: string | null
  updatedAt: string | null
}

export interface MeAccessiblePostDetail {
  id: number
  title: string
  slug: string
  summary: string | null
  contentHtml: string
  authorUsername: string
  authorNickname: string
  categoryName: string
  tagNames: string[]
  visibility: PostVisibility
  publishedAt: string | null
  updatedAt: string | null
}

export interface UserSearchResult {
  id: number
  username: string
  nickname: string
  avatarUrl: string | null
}

export interface GitHubImportRequest {
  token: string
  repo: string
  path: string
  branch?: string
}
```

- [ ] **Step 2: 更新 api/me.ts**

```typescript
export const searchUsers = async (q: string) => {
  const { data } = await http.get<UserSearchResult[]>('/me/users/search', { params: { q } })
  return data
}

export const fetchAccessiblePosts = async (params: PaginationParams = {}) => {
  const { data } = await http.get<PageResponse<MeAccessiblePostItem>>('/me/accessible-posts', { params })
  return data
}

export const fetchAccessiblePostDetail = async (id: number) => {
  const { data } = await http.get<MeAccessiblePostDetail>(`/me/accessible-posts/${id}`)
  return data
}

export const importFromGitHub = async (payload: GitHubImportRequest) => {
  const { data } = await http.post<MePostDetail>('/me/github-import', payload)
  return data
}
```

## Task 8: 前端 — 作者表单加可见性选择器

**Files:**
- Modify: `frontend/src/views/AuthorPostFormView.vue`
- New: `frontend/src/components/markdown/UserSearchDropdown.vue`

- [ ] **Step 1: 创建 UserSearchDropdown.vue**

功能：
- 输入框，输入时调用 searchUsers API
- 防抖 300ms
- 展示下拉搜索结果（username + nickname）
- 选中的用户显示为 tag/chip，可移除
- 支持多选
- Props: modelValue (string[]) — 已选中的 username 列表
- Emits: update:modelValue

- [ ] **Step 2: AuthorPostFormView.vue 添加可见性选择**

在"发布设置"区块增加可见性下拉选择器：
- PUBLIC / PRIVATE / RESTRICTED 三选一
- 当选择 RESTRICTED 时，显示 UserSearchDropdown 组件

在响应式 form 中新增：

```typescript
const form = reactive({
  // 现有字段...
  visibility: 'PUBLIC' as PostVisibility,
  allowedReaderUsernames: [] as string[],
})
```

编辑模式下（isEdit），从已有文章数据回填 `visibility` 和 `allowedReaderUsernames`。

提交时 `toPayload()` 包含这两个新字段。

## Task 9: 前端 — 可访问文章页 + GitHub 导入页 + 路由 + 工作区入口

**Files:**
- New: `frontend/src/views/AuthorAccessiblePostsView.vue`
- New: `frontend/src/views/AuthorGitHubImportView.vue`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/views/AuthorDashboardView.vue`

- [ ] **Step 1: 创建 AuthorAccessiblePostsView.vue**

展示当前用户有权限阅读的非公开文章列表。
复用现有 PostCard 样式的列表布局，点击跳转到 `/posts/:slug`（后端需在公开详情页也支持已认证用户查看有权限的非公开文章，或前端直接在新页面展示 — MVP 阶段可先跳转到现有 `/posts/:slug`）。

现实考虑：非公开文章在公开详情页无法访问，因此点击时应在当前列表页内展示内容，或跳转到一个专门的文章阅读页。简化方案：直接在当前页面用 `v-html` 展示 `contentHtml`。

- [ ] **Step 2: 创建 AuthorGitHubImportView.vue**

简单表单：
- Token 输入（type="password"，必填）
- 仓库地址输入（placeholder: `owner/repo`，必填）
- 文件路径输入（placeholder: `path/to/file.md`，必填）
- 分支输入（placeholder: "main"，可选）
- 提交按钮："导入"
- 成功提示 + 跳转到文章编辑链接

MVP 不实现预览，导入成功后直接跳转到作者编辑页。

- [ ] **Step 3: 更新 router/index.ts**

```typescript
{
  path: '/me/accessible-posts',
  name: 'author-accessible-posts',
  component: () => import('../views/AuthorAccessiblePostsView.vue'),
  meta: { requiresAuth: true },
},
{
  path: '/me/github-import',
  name: 'author-github-import',
  component: () => import('../views/AuthorGitHubImportView.vue'),
  meta: { requiresAuth: true },
},
```

- [ ] **Step 4: 更新 AuthorDashboardView.vue**

在作者工作区首页增加两个导航入口：
- "可访问文章" — 链接到 `/me/accessible-posts`
- "从 GitHub 导入" — 链接到 `/me/github-import`

## Task 10: 后端 — GitHub 导入服务

**Files:**
- New: `backend/src/main/java/com/example/blog/githubimport/GitHubImportService.java`
- New: `backend/src/main/java/com/example/blog/githubimport/GitHubImportController.java`
- New: `backend/src/main/java/com/example/blog/githubimport/GitHubImportRequest.java`
- New: `backend/src/main/java/com/example/blog/githubimport/GitHubImportRecord.java`
- New: `backend/src/main/resources/db/migration/V3__add_github_imports.sql`

- [ ] **Step 1: V3__add_github_imports.sql**

```sql
CREATE TABLE github_imports (
    id BIGSERIAL PRIMARY KEY,
    importer_id BIGINT NOT NULL REFERENCES users(id),
    repo VARCHAR(256) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    branch VARCHAR(64),
    post_id BIGINT REFERENCES posts(id),
    imported_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

- [ ] **Step 2: GitHubImportRecord 实体**

```java
@Entity
@Table(name = "github_imports")
@Getter @Setter
public class GitHubImportRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "importer_id", nullable = false)
    private User importer;
    
    @Column(nullable = false, length = 256)
    private String repo;
    
    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;
    
    @Column(length = 64)
    private String branch;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    
    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;
}
```

- [ ] **Step 3: GitHubImportRequest DTO**

```java
package com.example.blog.githubimport;

import jakarta.validation.constraints.NotBlank;

public record GitHubImportRequest(
        @NotBlank String token,
        @NotBlank String repo,
        @NotBlank String path,
        String branch
) {}
```

- [ ] **Step 4: GitHubImportService**

```java
@Service
public class GitHubImportService {
    
    private final GitHubImportRecordRepository gitHubImportRecordRepository;
    private final MePostService mePostService;
    private final MeService meService;
    private final RestTemplate restTemplate;

    public GitHubImportService(GitHubImportRecordRepository gitHubImportRecordRepository,
                                MePostService mePostService, MeService meService) {
        this.gitHubImportRecordRepository = gitHubImportRecordRepository;
        this.mePostService = mePostService;
        this.meService = meService;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public MePostDetailResponse importFromGitHub(GitHubImportRequest request) {
        // 1. 调用 GitHub API 获取文件内容
        String rawContent = fetchFileContent(request.token(), request.repo(), request.path(), request.branch());
        
        // 2. 解析 frontmatter 并提取正文
        String title = extractTitle(request.path(), rawContent);
        List<String> tagSlugs = extractTags(rawContent);
        String body = extractBody(rawContent);
        
        // 3. 组装并创建文章
        SaveMePostRequest postRequest = new SaveMePostRequest(
                title, null, null, body, null, getFirstCategorySlug(), tagSlugs,
                "PUBLIC", null
        );
        MePostDetailResponse created = mePostService.createMyPost(postRequest);
        
        // 4. 记录导入记录
        Post post = mePostService.getManagedPost(created.id());
        GitHubImportRecord record = new GitHubImportRecord();
        record.setImporter(meService.getCurrentUser());
        record.setRepo(request.repo());
        record.setFilePath(request.path());
        record.setBranch(request.branch());
        record.setPost(post);
        record.setImportedAt(LocalDateTime.now());
        gitHubImportRecordRepository.save(record);
        
        return created;
    }
    
    private String fetchFileContent(String token, String repo, String path, String branch) {
        String url = "https://api.github.com/repos/" + repo + "/contents/" + path;
        if (branch != null && !branch.isBlank()) {
            url += "?ref=" + branch;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Accept", "application/vnd.github.v3.raw");
        headers.set("User-Agent", "my-blog");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
    
    // 简易 frontmatter 解析（不引入 YAML 库）
    // 提取 --- 之间的 title/tags 行
}
```

- [ ] **Step 5: GitHubImportController**

```java
@RestController
@RequestMapping("/api/me")
public class GitHubImportController {
    
    private final GitHubImportService gitHubImportService;
    
    public GitHubImportController(GitHubImportService gitHubImportService) {
        this.gitHubImportService = gitHubImportService;
    }
    
    @PostMapping("/github-import")
    public MePostDetailResponse importPost(@Valid @RequestBody GitHubImportRequest request) {
        return gitHubImportService.importFromGitHub(request);
    }
}
```

- [ ] **Step 6: 创建 GitHubImportRecordRepository**

```java
public interface GitHubImportRecordRepository extends JpaRepository<GitHubImportRecord, Long> {
}
```

## Task 11: 文档更新

**Files:**
- Modify: `README.md`

- [ ] **Step 1: 更新 README.md**

在功能概览中增加：
- 作者工作区：新增"可访问文章"列表页
- 作者工作区：新增"从 GitHub 导入"功能
- 新增"文章可见性"说明

---

## 实现顺序

```
Task 1  (数据库迁移 V2 + PostVisibility 枚举)
    ↓
Task 2  (Post 实体加字段)
    ↓
Task 3  (DTO 扩展)
    ↓
Task 4  (服务层可见性处理 — MePostService + AdminPostService)
    ↓
Task 5  (公开 API 过滤 — PostRepository + PublicBlogService)
    ↓
Task 6  (用户搜索 + 可访问端点 — MeService, MeController, UserRepository)
    ↓
Task 7  (前端类型和 API)
    ↓
Task 8  (前端可见性 UI — 表单 + 用户搜索下拉)
    ↓
Task 9  (前端可访问文章页 + GitHub 导入页 + 路由)
    ↓
Task 10 (后端 GitHub 导入服务 + 迁移 V3)
    ↓
Task 11 (文档)
```

## 安全注意事项

- GitHub token 不可日志输出，前端传输用 POST body 而非 query param
- 非 PUBLIC 文章在公开接口返回 404 而非 403，避免信息泄露
- 可访问文章端点需校验当前用户身份
- RESTRICTED 文章的读者列表变更需同步更新 post_readers 表
- GitHub 导入的 frontmatter 解析不做 YAML 严格解析，只用简单的行解析提取 title/tags/slug
