package com.example.blog.repository;

import com.example.blog.model.PostTag;
import com.example.blog.model.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

    boolean existsByTagId(Long tagId);
}
