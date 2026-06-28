package com.example.blog.githubimport;

import com.example.blog.model.Post;
import com.example.blog.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_imports")
@Getter
@Setter
public class GitHubImportRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
