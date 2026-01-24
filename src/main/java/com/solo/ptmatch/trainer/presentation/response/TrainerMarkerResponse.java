package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.TrainerProfile;

// 개별 트레이너 마커 응답 DTO
public record TrainerMarkerResponse(
        Long trainerId,
        String trainerName,
        String facilityName,
        Double latitude,
        Double longitude,
        String profileImageUrl) {
    public static TrainerMarkerResponse of(
            Long trainerId,
            String trainerName,
            String facilityName,
            Double latitude,
            Double longitude,
            String profileImageUrl) {
        return new TrainerMarkerResponse(trainerId, trainerName, facilityName, latitude, longitude, profileImageUrl);
    }

    public static TrainerMarkerResponse from(TrainerProfile trainerProfile) {
        return new TrainerMarkerResponse(
                trainerProfile.getId(),
                trainerProfile.getUser().getName(),
                trainerProfile.getFacilityName(),
                trainerProfile.getLatitude(),
                trainerProfile.getLongitude(),
                trainerProfile.getProfileImageUrl());
    }
}
