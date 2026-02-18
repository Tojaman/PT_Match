package com.solo.ptmatch.payment.presentation.response;

import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import java.time.LocalDateTime;

public record PaymentPrepareScheduleResponse(
        Long availableScheduleId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {

    public static PaymentPrepareScheduleResponse from(AvailableSchedule schedule) {
        return new PaymentPrepareScheduleResponse(
                schedule.getId(),
                schedule.getStartTime(),
                schedule.getEndTime());
    }
}
