package com.solo.ptmatch.common.storage;

import com.solo.ptmatch.common.config.MinioProperties;
import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public PresignedUpload issuePresignedUpload(PresignedUploadCommand command) {

        String objectKey = buildObjectKey(command);
        int expirySeconds = toExpirySeconds(minioProperties.presignExpiry());

        try {
            GetPresignedObjectUrlArgs.Builder argsBuilder = GetPresignedObjectUrlArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(objectKey)
                    .method(Method.PUT)
                    .expiry(expirySeconds);

            if (StringUtils.hasText(command.contentType())) {
                argsBuilder.extraHeaders(Map.of("Content-Type", command.contentType()));
            }

            String uploadUrl = minioClient.getPresignedObjectUrl(argsBuilder.build());
            return new PresignedUpload(uploadUrl, objectKey, expirySeconds);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 presigned URL 요청, 파일 이름={}", command.originalFileName(), e);
            throw GlobalException.of(ErrorCode.INVALID_REQUEST);
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            log.error("presigned URL 발급 실패, 파일 이름={}", command.originalFileName(), e);
            throw GlobalException.of(ErrorCode.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    private String buildObjectKey(PresignedUploadCommand command) {
        String sanitizedName = sanitizeFileName(command.originalFileName());
        String extension = resolveExtension(sanitizedName, command.contentType());
        String directory = resolveDirectory(command);
        return "%s/%s%s".formatted(directory, UUID.randomUUID(), extension);
    }

    private String resolveDirectory(PresignedUploadCommand command) {
        return "%s/%d".formatted(command.directory(), command.ownerId());
    }

    private String sanitizeFileName(String fileName) {

        String decoded = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        String base = decoded.replace("\\", "/");
        int slashIndex = base.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < base.length() - 1) {
            base = base.substring(slashIndex + 1);
        }
        return base;
    }

    private String resolveExtension(String fileName, String contentType) {
        if (StringUtils.hasText(fileName)) {
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
                return fileName.substring(dotIndex);
            }
        }

        if (StringUtils.hasText(contentType)) {
            return switch (contentType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                case "image/gif" -> ".gif";
                default -> "";
            };
        }

        return "";
    }

    private int toExpirySeconds(Duration duration) {
        long seconds = duration.toSeconds();
        return Math.toIntExact(seconds);
    }
}
