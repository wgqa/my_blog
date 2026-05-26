package com.example.blog.auth;

import com.example.blog.auth.dto.MeUploadResponse;
import com.example.blog.config.StorageProperties;
import com.example.blog.exception.BadRequestException;
import com.example.blog.model.Upload;
import com.example.blog.model.User;
import com.example.blog.repository.UploadRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class MeUploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml"
    );

    private final MeService meService;
    private final UploadRepository uploadRepository;
    private final MinioClient minioClient;
    private final StorageProperties storageProperties;

    public MeUploadService(
            MeService meService,
            UploadRepository uploadRepository,
            MinioClient minioClient,
            StorageProperties storageProperties
    ) {
        this.meService = meService;
        this.uploadRepository = uploadRepository;
        this.minioClient = minioClient;
        this.storageProperties = storageProperties;
    }

    @Transactional
    public MeUploadResponse uploadImage(MultipartFile file) {
        validate(file);

        User currentUser = meService.getCurrentUser();
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename().trim();
        String extension = extractExtension(originalName);
        String storageKey = "authors/%d/%s%s".formatted(currentUser.getId(), UUID.randomUUID(), extension);
        String fileName = storageKey.substring(storageKey.lastIndexOf('/') + 1);

        try (InputStream inputStream = file.getInputStream()) {
            ensureBucket();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(storageProperties.bucket())
                            .object(storageKey)
                            .contentType(file.getContentType())
                            .stream(inputStream, file.getSize(), -1)
                            .build()
            );
        } catch (BadRequestException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("上传文件失败", exception);
        }

        Upload upload = new Upload();
        upload.setFileName(fileName);
        upload.setOriginalName(originalName);
        upload.setContentType(file.getContentType());
        upload.setFileSize(file.getSize());
        upload.setStorageKey(storageKey);
        upload.setUrl(resolvePublicUrl(storageKey));
        upload.setUploader(currentUser);
        upload.setCreatedAt(LocalDateTime.now());

        return toResponse(uploadRepository.save(upload));
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("上传文件不能为空");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new BadRequestException("文件名不能为空");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType().toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("仅支持上传图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("图片大小不能超过5MB");
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(storageProperties.bucket()).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(storageProperties.bucket()).build());
        }
    }

    private String extractExtension(String originalName) {
        int index = originalName.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return originalName.substring(index).toLowerCase(Locale.ROOT);
    }

    private String resolvePublicUrl(String storageKey) {
        String baseUrl = storageProperties.publicBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return storageProperties.endpoint() + "/" + storageProperties.bucket() + "/" + storageKey;
        }
        return baseUrl + "/" + storageKey;
    }

    private MeUploadResponse toResponse(Upload upload) {
        return new MeUploadResponse(
                upload.getId(),
                upload.getFileName(),
                upload.getOriginalName(),
                upload.getContentType(),
                upload.getFileSize(),
                upload.getUrl()
        );
    }
}
