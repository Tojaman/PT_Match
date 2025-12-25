package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;

public record LowSessionAlertDto(Long matchingId, String memberName, int remainingSessions) {
    public static LowSessionAlertDto from(Matching matching) {
        return new LowSessionAlertDto(
                matching.getId(),
                matching.getUser().getName(),
                matching.getRemainingSessions());
    }
}
