package com.solo.ptmatch.common.config;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        @NotBlank String endpoint,
        @NotBlank String bucket,
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        Duration presignExpiry
) {

    public Duration presignExpiry() {
        return presignExpiry;
    }
}
