package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "매칭 상세 정보")
public record MatchingDetailResponse(
    Long matchingId,
    String message,
    String status,
    MatchingUserInfo applicantInfo,
    MatchingTrainerInfo trainerInfo,
    List<MatchingScheduleInfo> schedules,
    ReviewInfo reviewInfo) {
    public static MatchingDetailResponse from(Matching matching, ReviewInfo reviewInfo) {
        return new MatchingDetailResponse(
            matching.getId(),
            matching.getMessage(),
            matching.getMatchingStatus().name(),
            MatchingUserInfo.from(matching.getMatchingUserInfo()),
            MatchingTrainerInfo.from(matching.getTrainerProfile()),
            matching.getSchedules().stream()
                    .map(MatchingScheduleInfo::from)
                    .toList(),
            reviewInfo);
    }
}
