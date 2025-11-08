package com.solo.ptmatch.product.presentation.response;

import com.solo.ptmatch.common.storage.PresignedUpload;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 이미지 업로드용 프리사인 URL 응답")
public record PresignedUrlResponse(
        @Schema(description = "이미지 업로드에 사용되는 프리사인 URL")
        String uploadUrl,
        @Schema(description = "업로드 완료 후 DB에 저장할 객체 키")
        String objectKey,
        @Schema(description = "프리사인 URL 만료까지 남은 시간(초)")
        long expiresInSeconds
) {

    public static PresignedUrlResponse from(PresignedUpload upload) {
        return new PresignedUrlResponse(upload.uploadUrl(), upload.objectKey(), upload.expirySeconds());
    }
}
