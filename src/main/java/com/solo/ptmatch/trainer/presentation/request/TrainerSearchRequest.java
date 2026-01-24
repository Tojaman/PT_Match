package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.location.domain.LocationType;
import com.solo.ptmatch.trainer.domain.SportType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 검색 조건")
public record TrainerSearchRequest(
        @Schema(description = "위치 타입 (FACILITY, SUBWAY, DISTRICT)", example = "SUBWAY")
        LocationType locationType,

        @Schema(description = "시설 이름 (FACILITY 타입일 때)", example = "강남 피트니스")
        String facilityName,

        @Schema(description = "법정동 코드 (DISTRICT 타입일 때)", example = "11110")
        String districtCode,

        @Schema(description = "중심 위도 (SUBWAY 타입일 때)", example = "37.5172")
        Double latitude,

        @Schema(description = "중심 경도 (SUBWAY 타입일 때)", example = "127.0473")
        Double longitude,

        @Schema(description = "검색 반경 km (SUBWAY 타입일 때, 기본값 3km)", example = "3.0")
        Double radiusKm,

        @Schema(description = "스포츠 종목 필터", example = "FITNESS")
        SportType sportType) {
}
