package com.bookstore.books.book.storage;

import com.bookstore.books.config.S3Properties;
import com.bookstore.books.exception.BadRequestException;
import com.bookstore.books.exception.StorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class BookCoverStorageService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public BookCoverStorageService(S3Client s3Client, S3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    public String uploadCover(UUID bookId, MultipartFile file) {
        validate(file);

        if (s3Properties.bucketName() == null || s3Properties.bucketName().isBlank()) {
            throw new StorageException("S3 bucket is not configured (set S3_BUCKET_NAME).");
        }
        if (s3Properties.region() == null || s3Properties.region().isBlank()) {
            throw new StorageException("AWS region is not configured (set AWS_REGION).");
        }

        String contentType = normalizeContentType(file.getContentType());
        String extension = EXTENSION_BY_CONTENT_TYPE.get(contentType);
        String key = "covers/%s-%s.%s".formatted(bookId, UUID.randomUUID(), extension);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Properties.bucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (S3Exception ex) {
            throw new StorageException("Failed to upload cover image to storage.", ex);
        } catch (IOException ex) {
            throw new StorageException("Failed to read uploaded cover image.", ex);
        }

        return "https://%s.s3.%s.amazonaws.com/%s"
                .formatted(s3Properties.bucketName(), s3Properties.region(), key);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Cover image file is required.");
        }

        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("Cover image must be 5MB or smaller.");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "Only JPEG, PNG, and WebP images are allowed (image/jpeg, image/png, image/webp)."
            );
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "";
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }
}
