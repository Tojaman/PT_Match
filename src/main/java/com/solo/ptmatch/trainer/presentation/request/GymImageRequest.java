package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.trainer.domain.GymImage;
import com.solo.ptmatch.trainer.domain.TrainerProfile;

public record GymImageRequest(
        String imageUrl,
        int displayOrder) {
    public GymImage toEntity(TrainerProfile savedProfile) {
        return GymImage.create(
                savedProfile,
                this.imageUrl,
                this.displayOrder);
    }
}
