package com.solo.ptmatch.location.presentation.dto;

import com.solo.ptmatch.location.domain.LegalDistrict;
import com.solo.ptmatch.location.domain.Location;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "위치 검색 결과")
public record LocationSearchResponse(
    @Schema(description = "위치 타입", example = "SUBWAY")
    String type,

    @Schema(description = "위치 이름", example = "강남역")
    String name,

    @Schema(description = "힌트 (지하철 노선 등)", example = "2호선")
    String hint,

    @Schema(description = "주소", example = "서울시 강남구")
    String address,

    @Schema(description = "법정동 코드 (DISTRICT 타입일 때만)", example = "1168010100")
    String districtCode,

    @Schema(description = "위도", example = "37.5116")
    Double latitude,

    @Schema(description = "경도", example = "127.0347")
    Double longitude) {

    public static LocationSearchResponse from(Location location) {
        return new LocationSearchResponse(
                location.getType().name(),
                location.getName(),
                location.getHint(),
                location.getAddress(),
                null,
                location.getLatitude(),
                location.getLongitude());
    }

    public static LocationSearchResponse from(TrainerProfile trainerProfile) {
        return new LocationSearchResponse(
                "GYM",
                trainerProfile.getGymName(),
                null,
                trainerProfile.getGymAddress(),
                null,
                trainerProfile.getGymLatitude(),
                trainerProfile.getGymLongitude());
    }

    public static LocationSearchResponse from(LegalDistrict district) {
        return new LocationSearchResponse(
                "DISTRICT",
                district.getName(),
                null,
                null,
                district.getCode(),
                district.getLatitude(),
                district.getLongitude());
    }
}
