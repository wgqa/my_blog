package com.example.blog.githubimport;

import com.example.blog.auth.MePostService;
import com.example.blog.auth.MeService;
import com.example.blog.auth.dto.MePostDetailResponse;
import com.example.blog.auth.dto.SaveMePostRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubImportService {

    private static final Pattern FRONTMATTER_PATTERN = Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n?", Pattern.DOTALL);
    private static final Pattern TITLE_PATTERN = Pattern.compile("(?m)^title\\s*:\\s*(.+)$");
    private static final Pattern TAGS_PATTERN = Pattern.compile("(?m)^tags\\s*:\\s*\\[(.*)\\]$");

    private final GitHubImportRecordRepository gitHubImportRecordRepository;
    private final MePostService mePostService;
    private final MeService meService;
    private final RestTemplate restTemplate;

    public GitHubImportService(
            GitHubImportRecordRepository gitHubImportRecordRepository,
            MePostService mePostService,
            MeService meService
    ) {
        this.gitHubImportRecordRepository = gitHubImportRecordRepository;
        this.mePostService = mePostService;
        this.meService = meService;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public MePostDetailResponse importFromGitHub(GitHubImportRequest request) {
        String rawContent = fetchFileContent(request.token(), request.repo(), request.path(), request.branch());

        String title = extractTitle(request.path(), rawContent);
        List<String> tagSlugs = extractTags(rawContent);
        String body = extractBody(rawContent);

        SaveMePostRequest postRequest = new SaveMePostRequest(
                title, null, null, body, null, getFirstCategorySlug(), tagSlugs,
                "PUBLIC", null
        );
        MePostDetailResponse created = mePostService.createMyPost(postRequest);

        GitHubImportRecord record = new GitHubImportRecord();
        record.setImporter(meService.getCurrentUser());
        record.setRepo(request.repo());
        record.setFilePath(URLDecoder.decode(request.path(), StandardCharsets.UTF_8));
        record.setBranch(request.branch());
        record.setPost(mePostService.getManagedPost(created.id()));
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

    private String extractTitle(String filePath, String rawContent) {
        Matcher fmMatcher = FRONTMATTER_PATTERN.matcher(rawContent);
        if (fmMatcher.find()) {
            Matcher titleMatcher = TITLE_PATTERN.matcher(fmMatcher.group(1));
            if (titleMatcher.find()) {
                return titleMatcher.group(1).trim();
            }
        }
        int lastSlash = filePath.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
        if (fileName.endsWith(".md")) {
            fileName = fileName.substring(0, fileName.length() - 3);
        }
        return fileName.replaceAll("[-_]", " ");
    }

    private List<String> extractTags(String rawContent) {
        Matcher fmMatcher = FRONTMATTER_PATTERN.matcher(rawContent);
        if (fmMatcher.find()) {
            Matcher tagsMatcher = TAGS_PATTERN.matcher(fmMatcher.group(1));
            if (tagsMatcher.find()) {
                String[] parts = tagsMatcher.group(1).split(",");
                List<String> tags = new ArrayList<>();
                for (String part : parts) {
                    String tag = part.trim().replaceAll("^[\"']|[\"']$", "");
                    if (!tag.isEmpty()) {
                        tags.add(tag);
                    }
                }
                return tags;
            }
        }
        return List.of();
    }

    private String extractBody(String rawContent) {
        Matcher fmMatcher = FRONTMATTER_PATTERN.matcher(rawContent);
        if (fmMatcher.find()) {
            return rawContent.substring(fmMatcher.end()).trim();
        }
        return rawContent.trim();
    }

    private String getFirstCategorySlug() {
        return mePostService.getFirstCategorySlug();
    }
}
