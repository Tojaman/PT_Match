package com.solo.ptmatch.payment.presentation.request;

import com.solo.ptmatch.matching.presentation.request.UserInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PaymentPrepareRequest(
        @NotNull
        Long trainerProfileId,

        @NotEmpty
        List<@NotNull Long> availableScheduleIds,

        @NotBlank
        @Size(max = 500)
        String message,

        @Valid
        @NotNull
        UserInfo userInfo
) {
}
