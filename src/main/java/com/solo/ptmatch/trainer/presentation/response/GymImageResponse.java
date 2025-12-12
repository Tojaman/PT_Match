package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.GymImage;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지점 이미지 응답")
public record GymImageResponse(
        @Schema(description = "이미지 URL", example = "https://example.com/gym1.jpg") String imageUrl,

        @Schema(description = "표시 순서", example = "1") int displayOrder) {
    public static GymImageResponse from(GymImage gymImage) {
        return new GymImageResponse(
                gymImage.getImageUrl(),
                gymImage.getDisplayOrder());
    }
}
