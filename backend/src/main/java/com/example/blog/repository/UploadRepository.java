package com.example.blog.repository;

import com.example.blog.model.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadRepository extends JpaRepository<Upload, Long> {

    List<Upload> findByUploaderId(Long uploaderId);
}
