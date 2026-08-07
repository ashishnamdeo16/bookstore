package com.bookstore.books.service;

import com.bookstore.books.book.storage.BookCoverStorageService;
import com.bookstore.books.config.S3Properties;
import com.bookstore.books.exception.BadRequestException;
import com.bookstore.books.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookCoverStorageServiceTest {

    @Mock
    private S3Client s3Client;

    private BookCoverStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new BookCoverStorageService(
                s3Client,
                new S3Properties("test-bucket", "us-west-2")
        );
    }

    @Test
    void GivenValidJpeg_WhenUploadCover_ThenPutObjectAndReturnPublicUrl() {
        UUID bookId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[]{1, 2, 3, 4});

        String url = storageService.uploadCover(bookId, file);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest putRequest = requestCaptor.getValue();
        assertThat(putRequest.bucket()).isEqualTo("test-bucket");
        assertThat(putRequest.key()).startsWith("covers/" + bookId + "-");
        assertThat(putRequest.key()).endsWith(".jpg");
        assertThat(putRequest.contentType()).isEqualTo("image/jpeg");
        assertThat(url).isEqualTo(
                "https://test-bucket.s3.us-west-2.amazonaws.com/" + putRequest.key());
    }

    @Test
    void GivenEmptyFile_WhenUploadCover_ThenThrowBadRequestException() {
        MockMultipartFile file = new MockMultipartFile("file", "cover.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> storageService.uploadCover(UUID.randomUUID(), file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cover image file is required.");
    }

    @Test
    void GivenUnsupportedContentType_WhenUploadCover_ThenThrowBadRequestException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.gif", "image/gif", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> storageService.uploadCover(UUID.randomUUID(), file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only JPEG, PNG, and WebP");
    }

    @Test
    void GivenFileLargerThanFiveMb_WhenUploadCover_ThenThrowBadRequestException() {
        byte[] payload = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", payload);

        assertThatThrownBy(() -> storageService.uploadCover(UUID.randomUUID(), file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cover image must be 5MB or smaller.");
    }

    @Test
    void GivenMissingBucket_WhenUploadCover_ThenThrowStorageException() {
        storageService = new BookCoverStorageService(s3Client, new S3Properties(" ", "us-west-2"));
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[]{1});

        assertThatThrownBy(() -> storageService.uploadCover(UUID.randomUUID(), file))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("S3 bucket is not configured");
    }

    @Test
    void GivenS3Failure_WhenUploadCover_ThenThrowStorageException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", new byte[]{1, 2});
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("denied").statusCode(403).build());

        assertThatThrownBy(() -> storageService.uploadCover(UUID.randomUUID(), file))
                .isInstanceOf(StorageException.class)
                .hasMessage("Failed to upload cover image to storage.");
    }
}
