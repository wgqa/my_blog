# Multi-User Blog MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个可通过 Docker Compose 启动的前后端分离多用户博客 MVP，支持游客浏览、作者发文、管理员管理。

**Architecture:** 采用单仓库 Monorepo，`frontend/` 承载前台博客、作者工作区与 `/admin` 管理后台，`backend/` 使用 Spring Boot 提供 JWT 认证、博客内容与后台管理 API。后端统一完成 Markdown→HTML 渲染与 HTML 清洗，图片通过 MinIO 对象存储管理，PostgreSQL 负责业务数据持久化。

**Tech Stack:** Vue 3, Vite, TypeScript, Tailwind CSS, Vue Router, Pinia, Axios, md-editor-v3, Vitest, Spring Boot 3, Spring Security, JPA, PostgreSQL, Flyway, MinIO, JUnit 5, Testcontainers, Docker Compose, Nginx

---

## File Structure Map

### Root
- Create: `docker-compose.yml` — 编排 frontend、backend、postgres、minio、nginx
- Create: `.env.example` — 前后端与基础设施环境变量模板
- Create: `.gitignore` — 忽略构建输出、环境变量、IDE 文件
- Modify: `README.md` — 项目介绍、开发启动、Docker Compose 启动、部署说明

### Backend
- Create: `backend/pom.xml` — Spring Boot 依赖与插件
- Create: `backend/src/main/resources/application.yml` — 应用基础配置
- Create: `backend/src/main/resources/application-dev.yml` — 本地开发配置
- Create: `backend/src/main/resources/db/migration/V1__init_schema.sql` — 初始化表结构
- Create: `backend/src/main/java/com/example/blog/BlogApplication.java`
- Create: `backend/src/main/java/com/example/blog/common/**` — 通用响应、异常、分页、slug 工具
- Create: `backend/src/main/java/com/example/blog/config/**` — CORS、OpenAPI、MinIO Bean、Markdown Bean
- Create: `backend/src/main/java/com/example/blog/security/**` — JWT 工具、过滤器、安全配置、UserDetails
- Create: `backend/src/main/java/com/example/blog/auth/**` — 登录 DTO、Service、Controller
- Create: `backend/src/main/java/com/example/blog/user/**` — 用户实体、Repository、作者资料、管理员用户管理
- Create: `backend/src/main/java/com/example/blog/post/**` — 文章实体、分类/标签关联、公开接口、作者接口、管理员接口
- Create: `backend/src/main/java/com/example/blog/category/**`
- Create: `backend/src/main/java/com/example/blog/tag/**`
- Create: `backend/src/main/java/com/example/blog/upload/**`
- Create: `backend/src/main/java/com/example/blog/stats/**`
- Create: `backend/src/test/java/com/example/blog/**` — service、controller、security、integration 测试
- Create: `backend/src/test/resources/application-test.yml`

### Frontend
- Create: `frontend/package.json` — Vite 项目依赖与 scripts
- Create: `frontend/vite.config.ts`
- Create: `frontend/tailwind.config.ts`
- Create: `frontend/postcss.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/style.css`
- Create: `frontend/src/router/index.ts` — 路由与守卫
- Create: `frontend/src/stores/auth.ts` — 登录态与角色判断
- Create: `frontend/src/api/http.ts` — Axios 实例与 token 注入
- Create: `frontend/src/api/*.ts` — auth、public、posts、admin、upload API
- Create: `frontend/src/layouts/PublicLayout.vue`
- Create: `frontend/src/layouts/UserLayout.vue`
- Create: `frontend/src/layouts/AdminLayout.vue`
- Create: `frontend/src/components/**` — 导航、文章卡片、搜索框、分页、Markdown 渲染容器
- Create: `frontend/src/views/public/**` — 首页、分类、搜索、详情、作者页
- Create: `frontend/src/views/user/**` — 登录、我的主页、我的文章、新建/编辑文章
- Create: `frontend/src/views/admin/**` — 仪表盘、用户管理、文章管理、分类标签管理
- Create: `frontend/src/utils/**` — 时间格式化、slug、表单常量
- Create: `frontend/src/types/**` — 用户、文章、分类、标签 DTO 类型
- Create: `frontend/src/test/**` — 页面与路由守卫测试

### Docker / Nginx
- Create: `docker/nginx/default.conf` — 前端静态资源与 `/api` 反向代理
- Create: `docker/backend/Dockerfile`
- Create: `docker/frontend/Dockerfile`
- Create: `docker/postgres/init/` — 如需种子数据脚本可后补，MVP 先依赖 Flyway

## Task 1: 初始化 Monorepo 与基础工具链

**Files:**
- Create: `.gitignore`
- Modify: `README.md`
- Create: `.env.example`
- Create: `frontend/package.json`
- Create: `backend/pom.xml`

- [ ] **Step 1: 写根目录忽略规则**

```gitignore
node_modules/
dist/
coverage/
.env
.env.*
!.env.example
.idea/
.vscode/
backend/target/
frontend/.vite/
frontend/node_modules/
minio-data/
postgres-data/
```

- [ ] **Step 2: 运行命令确认当前仓库状态**

Run: `git status --short`
Expected: 仅看到新增的计划文档与当前仓库已有文件，没有意外二进制文件。

- [ ] **Step 3: 写前端 package.json 最小骨架**

```json
{
  "name": "multi-user-blog-frontend",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc --noEmit && vite build",
    "preview": "vite preview",
    "test": "vitest run"
  },
  "dependencies": {
    "axios": "^1.7.2",
    "highlight.js": "^11.11.1",
    "md-editor-v3": "^4.21.3",
    "pinia": "^2.1.7",
    "vue": "^3.5.13",
    "vue-router": "^4.4.5"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.1.4",
    "autoprefixer": "^10.4.20",
    "postcss": "^8.4.49",
    "tailwindcss": "^3.4.14",
    "typescript": "^5.6.3",
    "vite": "^5.4.10",
    "vitest": "^2.1.3",
    "vue-tsc": "^2.1.8"
  }
}
```

- [ ] **Step 4: 写后端 pom.xml 最小骨架**

```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
  </parent>
  <groupId>com.example</groupId>
  <artifactId>blog</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <properties>
    <java.version>17</java.version>
  </properties>
  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
    <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.6</version></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>
    <dependency><groupId>io.minio</groupId><artifactId>minio</artifactId><version>8.5.12</version></dependency>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>2.6.0</version></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>junit-jupiter</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>postgresql</artifactId><scope>test</scope></dependency>
  </dependencies>
</project>
```

- [ ] **Step 5: 写环境变量模板**

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/blog
SPRING_DATASOURCE_USERNAME=blog
SPRING_DATASOURCE_PASSWORD=blog123
JWT_SECRET=change-this-secret-change-this-secret
JWT_EXPIRE_SECONDS=86400
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=blog-assets
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost
```

- [ ] **Step 6: 运行前后端依赖解析命令**

Run: `bash -lc "cd frontend && npm install && cd ../backend && ./mvnw -q -DskipTests help:effective-pom >/dev/null"`
Expected: `npm install` 成功，Maven 可解析依赖；若还未生成 `mvnw`，在 Task 2 中补齐 wrapper 后再重跑本步。

- [ ] **Step 7: 提交基础工程文件**

```bash
git add .gitignore .env.example README.md frontend/package.json backend/pom.xml
git commit -m "chore: initialize monorepo toolchain"
```

## Task 2: 搭建 Docker Compose、前端壳子与后端启动骨架

**Files:**
- Create: `docker-compose.yml`
- Create: `docker/backend/Dockerfile`
- Create: `docker/frontend/Dockerfile`
- Create: `docker/nginx/default.conf`
- Create: `backend/src/main/java/com/example/blog/BlogApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `frontend/vite.config.ts`
- Create: `frontend/index.html`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/style.css`

- [ ] **Step 1: 写后端启动类测试**

```java
@SpringBootTest
class BlogApplicationTests {
  @Test
  void contextLoads() {
  }
}
```

File: `backend/src/test/java/com/example/blog/BlogApplicationTests.java`

- [ ] **Step 2: 运行测试确认失败**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=BlogApplicationTests"`
Expected: FAIL，提示找不到 `BlogApplication` 或 Spring Boot 主类。

- [ ] **Step 3: 写后端主类与 application.yml**

```java
@SpringBootApplication
public class BlogApplication {
  public static void main(String[] args) {
    SpringApplication.run(BlogApplication.class, args);
  }
}
```

```yaml
server:
  port: 8080
spring:
  application:
    name: multi-user-blog
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
```

- [ ] **Step 4: 写前端入口壳子**

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './style.css'

createApp(App).use(createPinia()).mount('#app')
```

```vue
<template>
  <div class="min-h-screen bg-zinc-50 text-zinc-900">
    <main class="mx-auto max-w-5xl px-4 py-12">
      <h1 class="text-3xl font-bold">Multi-user Blog</h1>
      <p class="mt-3 text-zinc-600">Frontend shell is ready.</p>
    </main>
  </div>
</template>
```

- [ ] **Step 5: 写 Docker Compose 与 Dockerfiles**

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: blog
      POSTGRES_USER: blog
      POSTGRES_PASSWORD: blog123
    ports: ["5432:5432"]
    volumes: ["./postgres-data:/var/lib/postgresql/data"]
  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports: ["9000:9000", "9001:9001"]
    volumes: ["./minio-data:/data"]
  backend:
    build:
      context: .
      dockerfile: docker/backend/Dockerfile
    env_file: .env.example
    depends_on: [postgres, minio]
    ports: ["8080:8080"]
  frontend:
    build:
      context: .
      dockerfile: docker/frontend/Dockerfile
    environment:
      VITE_API_BASE_URL: http://localhost:8080/api
    ports: ["5173:5173"]
  nginx:
    image: nginx:1.27-alpine
    depends_on: [frontend, backend]
    volumes:
      - ./docker/nginx/default.conf:/etc/nginx/conf.d/default.conf:ro
    ports: ["80:80"]
```

- [ ] **Step 6: 运行测试和前端构建**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=BlogApplicationTests && cd ../frontend && npm run build"`
Expected: 后端测试 PASS，前端构建 PASS。

- [ ] **Step 7: 启动基础服务验证容器编排**

Run: `docker compose up -d postgres minio`
Expected: `postgres` 与 `minio` 均为 healthy/running，可通过 `docker compose ps` 看到服务状态。

- [ ] **Step 8: 提交基础运行骨架**

```bash
git add docker-compose.yml docker backend/src/main frontend/src frontend/index.html
git commit -m "chore: add runnable frontend and backend skeleton"
```

## Task 3: 建立数据库表结构、JPA 实体与仓储层

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__init_schema.sql`
- Create: `backend/src/main/java/com/example/blog/user/User.java`
- Create: `backend/src/main/java/com/example/blog/post/Post.java`
- Create: `backend/src/main/java/com/example/blog/category/Category.java`
- Create: `backend/src/main/java/com/example/blog/tag/Tag.java`
- Create: `backend/src/main/java/com/example/blog/post/PostTag.java`
- Create: `backend/src/main/java/com/example/blog/upload/Upload.java`
- Create: `backend/src/main/java/com/example/blog/**/repository/*.java`
- Test: `backend/src/test/java/com/example/blog/repository/SchemaSmokeTest.java`

- [ ] **Step 1: 写 Flyway schema smoke test**

```java
@Testcontainers
@SpringBootTest
class SchemaSmokeTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Test
  void flywayCreatesUsersPostsAndTagsTables(@Autowired JdbcTemplate jdbcTemplate) {
    Integer count = jdbcTemplate.queryForObject(
      "select count(*) from information_schema.tables where table_name in ('users','posts','categories','tags','post_tags','uploads')",
      Integer.class
    );
    assertThat(count).isEqualTo(6);
  }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=SchemaSmokeTest"`
Expected: FAIL，提示表不存在或 migration 缺失。

- [ ] **Step 3: 写初始化 SQL**

```sql
create table users (
  id bigserial primary key,
  username varchar(64) not null unique,
  password_hash varchar(255) not null,
  nickname varchar(64) not null,
  email varchar(128),
  avatar_url varchar(512),
  bio text,
  role varchar(16) not null,
  status varchar(16) not null,
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);

create table categories (
  id bigserial primary key,
  name varchar(64) not null unique,
  slug varchar(64) not null unique,
  description varchar(255),
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);
```

继续同文件补全 `posts`、`tags`、`post_tags`、`uploads` 表，并给 `posts.slug`、`tags.slug` 加 unique 约束。

- [ ] **Step 4: 写核心实体最小实现**

```java
@Entity
@Table(name = "users")
@Getter @Setter
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, length = 64)
  private String username;
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private UserRole role;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private UserStatus status;
}
```

`Post` 至少定义 `title`、`slug`、`summary`、`contentMarkdown`、`contentHtml`、`author`、`category`、`publishedAt`。

- [ ] **Step 5: 写 Repository 接口**

```java
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  boolean existsByUsername(String username);
}
```

为 `PostRepository` 添加：

```java
Optional<Post> findBySlugAndDeletedFalse(String slug);
Page<Post> findByDeletedFalse(Pageable pageable);
Page<Post> findByAuthorIdAndDeletedFalse(Long authorId, Pageable pageable);
```

- [ ] **Step 6: 运行 schema 测试**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=SchemaSmokeTest"`
Expected: PASS。

- [ ] **Step 7: 提交数据库基础层**

```bash
git add backend/src/main/resources/db/migration backend/src/main/java/com/example/blog
 git commit -m "feat: add blog schema and core entities"
```

## Task 4: 实现认证、安全配置与管理员种子账号

**Files:**
- Create: `backend/src/main/java/com/example/blog/security/JwtTokenService.java`
- Create: `backend/src/main/java/com/example/blog/security/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/example/blog/security/SecurityConfig.java`
- Create: `backend/src/main/java/com/example/blog/auth/AuthController.java`
- Create: `backend/src/main/java/com/example/blog/auth/AuthService.java`
- Create: `backend/src/main/java/com/example/blog/auth/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/example/blog/auth/dto/AuthResponse.java`
- Create: `backend/src/main/java/com/example/blog/user/AdminSeedRunner.java`
- Test: `backend/src/test/java/com/example/blog/auth/AuthControllerTest.java`
- Test: `backend/src/test/java/com/example/blog/security/SecurityAccessTest.java`

- [ ] **Step 1: 写登录接口测试**

```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean AuthService authService;

  @Test
  void loginReturnsJwtAndUserPayload() throws Exception {
    when(authService.login(any())).thenReturn(new AuthResponse("jwt-token", 1L, "admin", "管理员", "ADMIN"));

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"username":"admin","password":"password"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").value("jwt-token"))
      .andExpect(jsonPath("$.user.username").value("admin"));
  }
}
```

- [ ] **Step 2: 写未认证访问受限接口测试**

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {
  @Autowired MockMvc mockMvc;

  @Test
  void meEndpointRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/me/profile"))
      .andExpect(status().isUnauthorized());
  }
}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=AuthControllerTest,SecurityAccessTest"`
Expected: FAIL，缺少 controller/security 相关类。

- [ ] **Step 4: 写 JWT 与安全配置**

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
  return http
    .csrf(AbstractHttpConfigurer::disable)
    .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(c -> c
      .requestMatchers("/api/auth/**", "/api/public/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
      .requestMatchers("/api/admin/**").hasRole("ADMIN")
      .requestMatchers("/api/me/**").authenticated()
      .anyRequest().permitAll())
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
    .build();
}
```

- [ ] **Step 5: 写 AuthService 最小实现**

```java
public AuthResponse login(LoginRequest request) {
  User user = userRepository.findByUsername(request.username())
    .orElseThrow(() -> new UnauthorizedException("用户名或密码错误"));
  if (user.getStatus() == UserStatus.DISABLED) {
    throw new ForbiddenException("账号已被禁用");
  }
  if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
    throw new UnauthorizedException("用户名或密码错误");
  }
  String token = jwtTokenService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
  return AuthResponse.from(user, token);
}
```

- [ ] **Step 6: 写默认管理员种子逻辑**

```java
@Component
@RequiredArgsConstructor
public class AdminSeedRunner implements ApplicationRunner {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) {
    if (userRepository.existsByUsername("admin")) return;
    User admin = new User();
    admin.setUsername("admin");
    admin.setNickname("管理员");
    admin.setPasswordHash(passwordEncoder.encode("admin123456"));
    admin.setRole(UserRole.ADMIN);
    admin.setStatus(UserStatus.ENABLED);
    userRepository.save(admin);
  }
}
```

- [ ] **Step 7: 运行认证与安全测试**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=AuthControllerTest,SecurityAccessTest"`
Expected: PASS。

- [ ] **Step 8: 提交认证层**

```bash
git add backend/src/main/java/com/example/blog/security backend/src/main/java/com/example/blog/auth backend/src/test/java/com/example/blog
git commit -m "feat: add jwt authentication and security"
```

## Task 5: 实现公开内容查询、Markdown 渲染与作者主页 API

**Files:**
- Create: `backend/src/main/java/com/example/blog/post/PublicPostController.java`
- Create: `backend/src/main/java/com/example/blog/post/PublicPostService.java`
- Create: `backend/src/main/java/com/example/blog/post/MarkdownRenderer.java`
- Create: `backend/src/main/java/com/example/blog/post/dto/PublicPostSummaryResponse.java`
- Create: `backend/src/main/java/com/example/blog/post/dto/PublicPostDetailResponse.java`
- Create: `backend/src/main/java/com/example/blog/category/PublicCategoryController.java`
- Create: `backend/src/main/java/com/example/blog/user/PublicAuthorController.java`
- Test: `backend/src/test/java/com/example/blog/post/PublicPostControllerTest.java`

- [ ] **Step 1: 写文章详情公开接口测试**

```java
@WebMvcTest(PublicPostController.class)
class PublicPostControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean PublicPostService publicPostService;

  @Test
  void returnsPublishedPostBySlug() throws Exception {
    when(publicPostService.getPostBySlug("hello-world")).thenReturn(
      new PublicPostDetailResponse("hello-world", "Hello", "<h1>Hello</h1>", "摘要", "作者A", "java", List.of("spring"))
    );

    mockMvc.perform(get("/api/public/posts/hello-world"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.slug").value("hello-world"))
      .andExpect(jsonPath("$.contentHtml").value("<h1>Hello</h1>"));
  }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=PublicPostControllerTest"`
Expected: FAIL。

- [ ] **Step 3: 写 Markdown 渲染器接口与实现骨架**

```java
public interface MarkdownRenderer {
  String render(String markdown);
}

@Component
public class CommonmarkMarkdownRenderer implements MarkdownRenderer {
  @Override
  public String render(String markdown) {
    return Jsoup.clean(parser.render(markdown), Safelist.relaxed().addTags("pre", "code", "table", "thead", "tbody", "tr", "th", "td"));
  }
}
```

- [ ] **Step 4: 写公开文章查询实现**

```java
public PublicPostDetailResponse getPostBySlug(String slug) {
  Post post = postRepository.findBySlugAndDeletedFalse(slug)
    .orElseThrow(() -> new NotFoundException("文章不存在"));
  return PublicPostDetailResponse.from(post);
}
```

为列表接口添加分页、关键词搜索、分类筛选参数：`page`、`size`、`keyword`、`categorySlug`。

- [ ] **Step 5: 写作者主页与分类公开接口**

```java
@GetMapping("/api/public/authors/{username}")
public PublicAuthorResponse getAuthor(@PathVariable String username) { ... }
```

```java
@GetMapping("/api/public/categories/{slug}/posts")
public PageResponse<PublicPostSummaryResponse> getCategoryPosts(@PathVariable String slug, @RequestParam int page) { ... }
```

- [ ] **Step 6: 运行公开接口测试**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=PublicPostControllerTest"`
Expected: PASS。

- [ ] **Step 7: 提交公开阅读 API**

```bash
git add backend/src/main/java/com/example/blog/post backend/src/main/java/com/example/blog/category backend/src/main/java/com/example/blog/user
git commit -m "feat: add public blog reading APIs"
```

## Task 6: 实现作者资料、文章 CRUD 与图片上传 API

**Files:**
- Create: `backend/src/main/java/com/example/blog/post/AuthorPostController.java`
- Create: `backend/src/main/java/com/example/blog/post/AuthorPostService.java`
- Create: `backend/src/main/java/com/example/blog/post/dto/SavePostRequest.java`
- Create: `backend/src/main/java/com/example/blog/user/MeController.java`
- Create: `backend/src/main/java/com/example/blog/user/MeService.java`
- Create: `backend/src/main/java/com/example/blog/upload/UploadController.java`
- Create: `backend/src/main/java/com/example/blog/upload/UploadService.java`
- Test: `backend/src/test/java/com/example/blog/post/AuthorPostServiceTest.java`
- Test: `backend/src/test/java/com/example/blog/upload/UploadControllerTest.java`

- [ ] **Step 1: 写作者只能编辑自己文章的服务测试**

```java
@Test
void authorCannotUpdateAnotherAuthorsPost() {
  SavePostRequest request = new SavePostRequest("标题", "摘要", "# 正文", 1L, List.of(1L));
  when(postRepository.findById(9L)).thenReturn(Optional.of(postOwnedByOtherUser()));

  assertThatThrownBy(() -> authorPostService.updatePost(2L, 9L, request))
    .isInstanceOf(ForbiddenException.class)
    .hasMessage("无权编辑该文章");
}
```

- [ ] **Step 2: 写图片上传接口测试**

```java
@Test
void uploadImageReturnsObjectUrl() throws Exception {
  when(uploadService.uploadImage(anyLong(), any())).thenReturn(new UploadResponse("https://minio/blog/a.png"));

  mockMvc.perform(multipart("/api/me/uploads/images")
      .file(new MockMultipartFile("file", "a.png", "image/png", new byte[]{1,2,3}))
      .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUTHOR"))))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.url").value("https://minio/blog/a.png"));
}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=AuthorPostServiceTest,UploadControllerTest"`
Expected: FAIL。

- [ ] **Step 4: 写文章保存逻辑**

```java
public Post saveNewPost(Long authorId, SavePostRequest request) {
  User author = userRepository.getReferenceById(authorId);
  Category category = categoryRepository.findById(request.categoryId())
    .orElseThrow(() -> new NotFoundException("分类不存在"));

  Post post = new Post();
  post.setTitle(request.title());
  post.setSlug(slugService.createUniqueSlug(request.title()));
  post.setSummary(request.summary());
  post.setContentMarkdown(request.contentMarkdown());
  post.setContentHtml(markdownRenderer.render(request.contentMarkdown()));
  post.setAuthor(author);
  post.setCategory(category);
  post.setPublishedAt(OffsetDateTime.now());
  return postRepository.save(post);
}
```

- [ ] **Step 5: 写我的资料接口**

```java
@GetMapping("/api/me/profile")
public MeProfileResponse profile(@AuthenticationPrincipal BlogUserPrincipal principal) {
  return meService.getProfile(principal.userId());
}

@PutMapping("/api/me/profile")
public MeProfileResponse update(@AuthenticationPrincipal BlogUserPrincipal principal, @Valid @RequestBody UpdateProfileRequest request) {
  return meService.updateProfile(principal.userId(), request);
}
```

- [ ] **Step 6: 写图片上传服务**

```java
public UploadResponse uploadImage(Long uploaderId, MultipartFile file) {
  if (!Set.of("image/png", "image/jpeg", "image/webp", "image/gif").contains(file.getContentType())) {
    throw new BadRequestException("仅支持常见图片格式");
  }
  String objectKey = "uploads/" + uploaderId + "/" + UUID.randomUUID() + extension(file.getOriginalFilename());
  minioClient.putObject(PutObjectArgs.builder()
    .bucket(bucket)
    .object(objectKey)
    .stream(file.getInputStream(), file.getSize(), -1)
    .contentType(file.getContentType())
    .build());
  return new UploadResponse(publicUrlBase + "/" + bucket + "/" + objectKey);
}
```

- [ ] **Step 7: 运行作者与上传测试**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=AuthorPostServiceTest,UploadControllerTest"`
Expected: PASS。

- [ ] **Step 8: 提交作者工作区 API**

```bash
git add backend/src/main/java/com/example/blog/post backend/src/main/java/com/example/blog/upload backend/src/main/java/com/example/blog/user
git commit -m "feat: add author post management and image upload"
```

## Task 7: 实现管理员用户、文章、分类、标签、统计 API

**Files:**
- Create: `backend/src/main/java/com/example/blog/user/AdminUserController.java`
- Create: `backend/src/main/java/com/example/blog/user/AdminUserService.java`
- Create: `backend/src/main/java/com/example/blog/category/AdminCategoryController.java`
- Create: `backend/src/main/java/com/example/blog/tag/AdminTagController.java`
- Create: `backend/src/main/java/com/example/blog/post/AdminPostController.java`
- Create: `backend/src/main/java/com/example/blog/stats/AdminStatsController.java`
- Test: `backend/src/test/java/com/example/blog/user/AdminUserControllerTest.java`
- Test: `backend/src/test/java/com/example/blog/stats/AdminStatsControllerTest.java`

- [ ] **Step 1: 写管理员创建作者账号接口测试**

```java
@Test
void adminCreatesAuthorAccount() throws Exception {
  when(adminUserService.createUser(any())).thenReturn(new AdminUserResponse(2L, "writer", "作者A", "AUTHOR", "ENABLED"));

  mockMvc.perform(post("/api/admin/users")
      .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {"username":"writer","nickname":"作者A","password":"writer123456","role":"AUTHOR"}
      """))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.username").value("writer"));
}
```

- [ ] **Step 2: 写基础统计测试**

```java
@Test
void returnsDashboardCounts() throws Exception {
  when(adminStatsService.getStats()).thenReturn(new DashboardStatsResponse(3L, 10L, 4L, 7L));

  mockMvc.perform(get("/api/admin/stats").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.userCount").value(3))
    .andExpect(jsonPath("$.postCount").value(10));
}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=AdminUserControllerTest,AdminStatsControllerTest"`
Expected: FAIL。

- [ ] **Step 4: 写管理员用户管理最小实现**

```java
public AdminUserResponse createUser(CreateAdminUserRequest request) {
  if (userRepository.existsByUsername(request.username())) {
    throw new BadRequestException("用户名已存在");
  }
  User user = new User();
  user.setUsername(request.username());
  user.setNickname(request.nickname());
  user.setPasswordHash(passwordEncoder.encode(request.password()));
  user.setRole(request.role());
  user.setStatus(UserStatus.ENABLED);
  return AdminUserResponse.from(userRepository.save(user));
}
```

- [ ] **Step 5: 写管理员内容管理接口**

```java
@DeleteMapping("/api/admin/posts/{id}")
public void deletePost(@PathVariable Long id) {
  adminPostService.deletePost(id);
}
```

为分类与标签分别实现列表、新建、编辑、删除四组接口；删除前检查是否被文章引用，若被引用则返回 400。

- [ ] **Step 6: 写统计查询实现**

```java
public DashboardStatsResponse getStats() {
  return new DashboardStatsResponse(
    userRepository.count(),
    postRepository.countByDeletedFalse(),
    categoryRepository.count(),
    tagRepository.count()
  );
}
```

- [ ] **Step 7: 运行管理员接口测试**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=AdminUserControllerTest,AdminStatsControllerTest"`
Expected: PASS。

- [ ] **Step 8: 提交后台管理 API**

```bash
git add backend/src/main/java/com/example/blog/user backend/src/main/java/com/example/blog/category backend/src/main/java/com/example/blog/tag backend/src/main/java/com/example/blog/post backend/src/main/java/com/example/blog/stats
git commit -m "feat: add admin management APIs"
```

## Task 8: 初始化前端路由、布局、鉴权 Store 与 API 客户端

**Files:**
- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/stores/auth.ts`
- Create: `frontend/src/api/http.ts`
- Create: `frontend/src/layouts/PublicLayout.vue`
- Create: `frontend/src/layouts/UserLayout.vue`
- Create: `frontend/src/layouts/AdminLayout.vue`
- Create: `frontend/src/views/user/LoginView.vue`
- Test: `frontend/src/test/router.spec.ts`

- [ ] **Step 1: 写路由守卫测试**

```ts
it('redirects guest away from /me/posts', async () => {
  const router = createRouter({ history: createMemoryHistory(), routes })
  const auth = useAuthStore()
  auth.token = null
  await router.push('/me/posts')
  expect(router.currentRoute.value.fullPath).toBe('/login')
})
```

- [ ] **Step 2: 运行前端测试确认失败**

Run: `bash -lc "cd frontend && npm run test -- router.spec.ts"`
Expected: FAIL。

- [ ] **Step 3: 写 Axios 客户端与 token 注入**

```ts
const http = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL })
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('blog-token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
export default http
```

- [ ] **Step 4: 写 auth store**

```ts
export const useAuthStore = defineStore('auth', {
  state: () => ({ token: localStorage.getItem('blog-token') as string | null, user: null as AuthUser | null }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    setSession(payload: LoginResponse) {
      this.token = payload.token
      this.user = payload.user
      localStorage.setItem('blog-token', payload.token)
    },
    clearSession() {
      this.token = null
      this.user = null
      localStorage.removeItem('blog-token')
    }
  }
})
```

- [ ] **Step 5: 写路由与布局骨架**

```ts
const routes = [
  { path: '/', component: () => import('../views/public/HomeView.vue'), meta: { layout: 'public' } },
  { path: '/login', component: () => import('../views/user/LoginView.vue'), meta: { guestOnly: true, layout: 'public' } },
  { path: '/me/posts', component: () => import('../views/user/MyPostsView.vue'), meta: { requiresAuth: true, layout: 'user' } },
  { path: '/admin', component: () => import('../views/admin/AdminDashboardView.vue'), meta: { requiresAdmin: true, layout: 'admin' } }
]
```

- [ ] **Step 6: 运行前端测试**

Run: `bash -lc "cd frontend && npm run test -- router.spec.ts"`
Expected: PASS。

- [ ] **Step 7: 提交前端基础框架**

```bash
git add frontend/src/router frontend/src/stores frontend/src/api frontend/src/layouts frontend/src/views/user/LoginView.vue frontend/src/test/router.spec.ts
git commit -m "feat: add frontend routing and auth shell"
```

## Task 9: 实现公共前台页面与阅读体验

**Files:**
- Create: `frontend/src/api/public.ts`
- Create: `frontend/src/components/AppHeader.vue`
- Create: `frontend/src/components/PostCard.vue`
- Create: `frontend/src/components/MarkdownContent.vue`
- Create: `frontend/src/views/public/HomeView.vue`
- Create: `frontend/src/views/public/PostDetailView.vue`
- Create: `frontend/src/views/public/CategoryListView.vue`
- Create: `frontend/src/views/public/CategoryPostsView.vue`
- Create: `frontend/src/views/public/SearchView.vue`
- Create: `frontend/src/views/public/AuthorProfileView.vue`
- Test: `frontend/src/test/public-pages.spec.ts`

- [ ] **Step 1: 写首页列表渲染测试**

```ts
it('renders post cards from API data', async () => {
  vi.spyOn(publicApi, 'fetchPosts').mockResolvedValue({ items: [{ slug: 'hello', title: 'Hello', summary: '摘要', authorName: '作者A' }], total: 1 })
  const wrapper = mount(HomeView)
  await flushPromises()
  expect(wrapper.text()).toContain('Hello')
  expect(wrapper.text()).toContain('作者A')
})
```

- [ ] **Step 2: 写文章详情渲染测试**

```ts
it('renders sanitized html inside markdown container', async () => {
  vi.spyOn(publicApi, 'fetchPostDetail').mockResolvedValue({ title: 'Hello', contentHtml: '<pre><code>code</code></pre>' })
  const wrapper = mount(PostDetailView, { global: routerGlobalsFor('/posts/hello') })
  await flushPromises()
  expect(wrapper.find('[data-testid="markdown-content"]').html()).toContain('<code>code</code>')
})
```

- [ ] **Step 3: 运行测试确认失败**

Run: `bash -lc "cd frontend && npm run test -- public-pages.spec.ts"`
Expected: FAIL。

- [ ] **Step 4: 写公共 API 与首页页面**

```ts
export const fetchPosts = (params: PostQuery) => http.get('/public/posts', { params }).then(r => r.data)
export const fetchPostDetail = (slug: string) => http.get(`/public/posts/${slug}`).then(r => r.data)
```

```vue
<template>
  <section class="space-y-6">
    <PostCard v-for="post in posts.items" :key="post.slug" :post="post" />
  </section>
</template>
```

- [ ] **Step 5: 写 MarkdownContent 组件**

```vue
<template>
  <article
    data-testid="markdown-content"
    class="prose prose-zinc max-w-none prose-pre:overflow-x-auto prose-img:rounded-lg"
    v-html="html"
  />
</template>

<script setup lang="ts">
defineProps<{ html: string }>()
</script>
```

- [ ] **Step 6: 写搜索、分类、作者页**

每个页面都从 `publicApi` 拉取数据，查询参数通过 `useRoute().query` 或 `params.slug` 读取；保持列表布局一致，不引入额外筛选器复杂度。

- [ ] **Step 7: 运行公共页面测试与构建**

Run: `bash -lc "cd frontend && npm run test -- public-pages.spec.ts && npm run build"`
Expected: 测试 PASS，构建 PASS。

- [ ] **Step 8: 提交前台阅读页**

```bash
git add frontend/src/api/public.ts frontend/src/components frontend/src/views/public frontend/src/test/public-pages.spec.ts
git commit -m "feat: add public blog pages"
```

## Task 10: 实现作者工作区、Markdown 编辑器与个人资料页

**Files:**
- Create: `frontend/src/api/me.ts`
- Create: `frontend/src/api/upload.ts`
- Create: `frontend/src/views/user/MyPostsView.vue`
- Create: `frontend/src/views/user/PostEditorView.vue`
- Create: `frontend/src/views/user/MyProfileView.vue`
- Create: `frontend/src/components/PostEditorForm.vue`
- Test: `frontend/src/test/author-workspace.spec.ts`

- [ ] **Step 1: 写登录成功后写入 store 的测试**

```ts
it('stores token after successful login', async () => {
  vi.spyOn(authApi, 'login').mockResolvedValue({ token: 'jwt', user: { username: 'writer', role: 'AUTHOR' } })
  const wrapper = mount(LoginView)
  await wrapper.find('input[name="username"]').setValue('writer')
  await wrapper.find('input[name="password"]').setValue('writer123456')
  await wrapper.find('form').trigger('submit.prevent')
  expect(useAuthStore().token).toBe('jwt')
})
```

- [ ] **Step 2: 写文章编辑器页面测试**

```ts
it('submits markdown content to save endpoint', async () => {
  vi.spyOn(meApi, 'createPost').mockResolvedValue({ id: 1, title: 'Hello' })
  const wrapper = mount(PostEditorView)
  await wrapper.find('input[name="title"]').setValue('Hello')
  await wrapper.find('[data-testid="editor-markdown"]').setValue('# Hello')
  await wrapper.find('form').trigger('submit.prevent')
  expect(meApi.createPost).toHaveBeenCalledWith(expect.objectContaining({ contentMarkdown: '# Hello' }))
})
```

- [ ] **Step 3: 运行测试确认失败**

Run: `bash -lc "cd frontend && npm run test -- author-workspace.spec.ts"`
Expected: FAIL。

- [ ] **Step 4: 写作者 API 封装**

```ts
export const fetchMyPosts = () => http.get('/me/posts').then(r => r.data)
export const createPost = (payload: SavePostPayload) => http.post('/me/posts', payload).then(r => r.data)
export const updateProfile = (payload: UpdateProfilePayload) => http.put('/me/profile', payload).then(r => r.data)
```

- [ ] **Step 5: 写编辑器表单**

```vue
<MdEditor
  v-model="form.contentMarkdown"
  data-testid="editor-markdown"
  :toolbars="['bold','italic','title','link','image','code','preview']"
  @onUploadImg="handleUploadImg"
/>
```

`handleUploadImg` 中调用 `/api/me/uploads/images`，把返回的 URL 注入编辑器。

- [ ] **Step 6: 写我的文章与我的资料页**

`MyPostsView.vue` 负责文章列表、新建按钮、编辑入口、删除操作；`MyProfileView.vue` 负责昵称、邮箱、简介、头像地址编辑。

- [ ] **Step 7: 运行作者工作区测试**

Run: `bash -lc "cd frontend && npm run test -- author-workspace.spec.ts"`
Expected: PASS。

- [ ] **Step 8: 提交作者前端功能**

```bash
git add frontend/src/api/me.ts frontend/src/api/upload.ts frontend/src/views/user frontend/src/components/PostEditorForm.vue frontend/src/test/author-workspace.spec.ts
git commit -m "feat: add author workspace frontend"
```

## Task 11: 实现管理员后台页面

**Files:**
- Create: `frontend/src/api/admin.ts`
- Create: `frontend/src/views/admin/AdminDashboardView.vue`
- Create: `frontend/src/views/admin/AdminUsersView.vue`
- Create: `frontend/src/views/admin/AdminPostsView.vue`
- Create: `frontend/src/views/admin/AdminCategoriesView.vue`
- Create: `frontend/src/views/admin/AdminTagsView.vue`
- Test: `frontend/src/test/admin-pages.spec.ts`

- [ ] **Step 1: 写管理员仪表盘测试**

```ts
it('renders stats cards', async () => {
  vi.spyOn(adminApi, 'fetchStats').mockResolvedValue({ userCount: 3, postCount: 10, categoryCount: 4, tagCount: 7 })
  const wrapper = mount(AdminDashboardView)
  await flushPromises()
  expect(wrapper.text()).toContain('10')
  expect(wrapper.text()).toContain('文章总数')
})
```

- [ ] **Step 2: 写用户管理禁用按钮测试**

```ts
it('calls disable user action', async () => {
  vi.spyOn(adminApi, 'updateUserStatus').mockResolvedValue({ id: 2, status: 'DISABLED' })
  const wrapper = mount(AdminUsersView)
  await wrapper.find('[data-testid="disable-user-2"]').trigger('click')
  expect(adminApi.updateUserStatus).toHaveBeenCalledWith(2, 'DISABLED')
})
```

- [ ] **Step 3: 运行测试确认失败**

Run: `bash -lc "cd frontend && npm run test -- admin-pages.spec.ts"`
Expected: FAIL。

- [ ] **Step 4: 写后台 API 封装**

```ts
export const fetchStats = () => http.get('/admin/stats').then(r => r.data)
export const fetchUsers = () => http.get('/admin/users').then(r => r.data)
export const updateUserStatus = (id: number, status: 'ENABLED' | 'DISABLED') => http.patch(`/admin/users/${id}/status`, { status }).then(r => r.data)
```

- [ ] **Step 5: 写后台管理页面**

- `AdminDashboardView.vue`：4 个统计卡片
- `AdminUsersView.vue`：用户表格、创建作者弹窗、启用/禁用按钮
- `AdminPostsView.vue`：全站文章表格、编辑入口、删除按钮
- `AdminCategoriesView.vue` / `AdminTagsView.vue`：列表 + 新建/编辑表单

保持布局极简，不引入 UI 组件库；直接用 Tailwind 表单与表格样式。

- [ ] **Step 6: 运行后台页面测试与构建**

Run: `bash -lc "cd frontend && npm run test -- admin-pages.spec.ts && npm run build"`
Expected: 测试 PASS，构建 PASS。

- [ ] **Step 7: 提交后台前端**

```bash
git add frontend/src/api/admin.ts frontend/src/views/admin frontend/src/test/admin-pages.spec.ts
git commit -m "feat: add admin dashboard frontend"
```

## Task 12: 完善 README、端到端手工验证与 Docker 联调

**Files:**
- Modify: `README.md`
- Modify: `docker-compose.yml`
- Modify: `docker/nginx/default.conf`
- Test: `backend/src/test/java/com/example/blog/integration/BlogSmokeIntegrationTest.java`

- [ ] **Step 1: 写基础联调 smoke test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BlogSmokeIntegrationTest {
  @Test
  void publicPostsEndpointReturnsOk(@Autowired TestRestTemplate restTemplate) {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/public/posts", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
```

- [ ] **Step 2: 运行测试确认通过**

Run: `bash -lc "cd backend && ./mvnw test -Dtest=BlogSmokeIntegrationTest"`
Expected: PASS。

- [ ] **Step 3: 写 README 运行说明**

README 至少包含以下章节：

```md
## 项目简介
## 技术栈
## 目录结构
## 本地开发
## Docker Compose 启动
## 默认管理员账号
## MinIO 使用说明
## 生产部署建议
```

默认管理员说明明确写出：首次启动后用户名 `admin`，密码 `admin123456`，并提醒首次登录后修改。

- [ ] **Step 4: 启动完整服务联调**

Run: `docker compose up --build`
Expected: 
- `http://localhost:5173` 可打开前端
- `http://localhost:8080/swagger-ui/index.html` 可打开接口文档
- `http://localhost:9001` 可打开 MinIO Console
- 登录 `admin/admin123456` 成功

- [ ] **Step 5: 执行手工验证清单**

按顺序验证：
1. 游客浏览首页、文章详情、搜索、分类页。
2. 管理员创建一个作者账号。
3. 作者登录并创建一篇带代码块和图片的文章。
4. 游客打开该文章，确认 Markdown、代码高亮、图片样式正常。
5. 管理员禁用该作者，确认该作者无法再次登录。
6. 管理员在后台删除文章，确认首页列表同步消失。

- [ ] **Step 6: 运行最终测试集合**

Run: `bash -lc "cd backend && ./mvnw test && cd ../frontend && npm run test && npm run build"`
Expected: 全部 PASS。

- [ ] **Step 7: 提交交付文档与联调结果**

```bash
git add README.md docker-compose.yml docker/nginx/default.conf backend/src/test/java/com/example/blog/integration
git commit -m "docs: finalize setup and verification guide"
```

## Self-Review

### Spec coverage
- 游客浏览、分类筛选、搜索、详情、作者页：Task 5、Task 9 覆盖
- 作者登录、发文、编辑、删除、图片上传、个人主页：Task 4、Task 6、Task 10 覆盖
- 管理员用户/文章/分类/标签/统计：Task 7、Task 11 覆盖
- Markdown 渲染、代码高亮、图片适配：Task 5、Task 9、Task 10 覆盖
- Docker Compose、README、部署说明：Task 2、Task 12 覆盖

### Placeholder scan
- 未使用 TBD/TODO/“类似 Task N” 这类占位描述。
- 每个任务包含文件、命令与关键代码片段。

### Type consistency
- 统一使用 `AUTHOR` / `ADMIN`、`ENABLED` / `DISABLED`、`contentMarkdown` / `contentHtml` 命名。
- 前端 token key 统一为 `blog-token`。

### Scope check
- 计划覆盖完整 MVP，但按基础骨架 → 核心博客 → 后台管理三阶段推进；每一阶段都可形成可运行增量。
