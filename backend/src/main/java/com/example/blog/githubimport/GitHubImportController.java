package com.example.blog.githubimport;

import com.example.blog.auth.dto.MePostDetailResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
