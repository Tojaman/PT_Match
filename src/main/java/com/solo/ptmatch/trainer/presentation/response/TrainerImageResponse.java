package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.TrainerImage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 이미지 응답")
public record TrainerImageResponse(
        @Schema(description = "이미지 URL", example = "https://example.com/trainer1.jpg") String imageUrl,

        @Schema(description = "표시 순서", example = "1") int displayOrder) {
    public static TrainerImageResponse from(TrainerImage trainerImage) {
        return new TrainerImageResponse(
                trainerImage.getImageUrl(),
                trainerImage.getDisplayOrder());
    }
}
