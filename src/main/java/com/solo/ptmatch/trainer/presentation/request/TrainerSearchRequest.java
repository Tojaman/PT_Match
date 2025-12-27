package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.location.domain.LocationType;
import com.solo.ptmatch.trainer.domain.Specialty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 검색 조건")
public record TrainerSearchRequest(
        @Schema(description = "위치 타입 (GYM, SUBWAY, ADDRESS)", example = "SUBWAY") LocationType locationType,

        @Schema(description = "헬스장 이름 (GYM 타입일 때)", example = "휘트니스엠 학동점") String gymName,

        @Schema(description = "중심 위도 (SUBWAY/ADDRESS 타입일 때)", example = "37.5172") Double latitude,

        @Schema(description = "중심 경도 (SUBWAY/ADDRESS 타입일 때)", example = "127.0473") Double longitude,

        @Schema(description = "검색 반경 km (기본값 3km)", example = "3.0") Double radiusKm,

        @Schema(description = "전문 분야 필터", example = "BODYBUILDING") Specialty specialty) {
}
