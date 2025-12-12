package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.trainer.domain.TrainerImage;
import com.solo.ptmatch.trainer.domain.TrainerProfile;

public record TrainerImageRequest(
        String imageUrl,
        int displayOrder) {
    public TrainerImage toEntity(TrainerProfile savedProfile) {
        return TrainerImage.create(
                savedProfile,
                this.imageUrl,
                this.displayOrder);
    }
}
