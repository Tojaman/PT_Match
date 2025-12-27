package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;

@Schema(description = "트레이너 프로필 등록 요청")
public record TrainerProfileUpsertRequest(
    @Schema(description = "자기소개", example = "10년 경력의 전문 트레이너입니다.")
    @NotBlank
    String bio,

    @Schema(description = "경력 년수", example = "10")
    @Min(0)
    int careerYears,

    @Schema(description = "전문 분야 목록", example = "[\"DIET\", \"REHABILITATION\"]")
    @NotNull
    @NotEmpty
    List<Specialty> specialties,

    @Schema(description = "지점 이름", example = "강남역")
    @NotBlank
    String gymName,

    @Schema(description = "활동 지점", example = "서울 강남구 ...")
    @NotBlank
    String gymAddress,

    @Schema(description = "지점 위도", example = "37.5116")
    @NotNull
    double gymLatitude,

    @Schema(description = "지점 경도", example = "127.0667")
    @NotNull
    double gymLongitude,

    @Schema(description = "트레이너 이미지 목록")
    @NotNull
    @NotEmpty
    List<TrainerImageRequest> trainerImages,

    @Schema(description = "지점 이미지 목록")
    @NotNull
    @NotEmpty
    List<GymImageRequest> gymImages,

    @Schema(description = "자격증 목록")
    @NotNull
    @Valid
    List<TrainerCertificationRequest> certifications,

    @Schema(description = "회당 가격 (단위: 원)", example = "50000")
    @Min(0)
    Integer pricePerSession) {

    public TrainerProfile toEntity(User user) {
        return TrainerProfile.create(
                user,
                bio,
                careerYears,
                new HashSet<>(specialties),
                gymName,
                gymAddress,
                gymLatitude,
                gymLongitude,
                trainerImages.get(0).imageUrl(),
                pricePerSession);
    }
}
