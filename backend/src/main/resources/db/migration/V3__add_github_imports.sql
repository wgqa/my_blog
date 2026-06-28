CREATE TABLE github_imports (
    id BIGSERIAL PRIMARY KEY,
    importer_id BIGINT NOT NULL REFERENCES users(id),
    repo VARCHAR(256) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    branch VARCHAR(64),
    post_id BIGINT REFERENCES posts(id),
    imported_at TIMESTAMP NOT NULL DEFAULT NOW()
);
