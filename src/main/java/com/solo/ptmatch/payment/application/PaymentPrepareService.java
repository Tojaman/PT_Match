package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.payment.domain.PaymentOrder;
import com.solo.ptmatch.payment.infrastructure.PaymentOrderRepository;
import com.solo.ptmatch.payment.infrastructure.PaymentProperties;
import com.solo.ptmatch.payment.presentation.request.PaymentPrepareRequest;
import com.solo.ptmatch.payment.presentation.response.PaymentPrepareResponse;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentPrepareService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final AvailableScheduleRepository availableScheduleRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentProperties paymentProperties;

    @Transactional
    public PaymentPrepareResponse prepare(String userEmail, PaymentPrepareRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findById(request.trainerProfileId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        List<AvailableSchedule> schedules = availableScheduleRepository.findAllByIdInAndTrainerProfileId(
                request.availableScheduleIds(),
                trainerProfile.getId());

        if (schedules.stream().anyMatch(schedule -> schedule.getReservationStatus() != ReservationStatus.AVAILABLE)) {
            throw GlobalException.of(ErrorCode.SCHEDULE_ALREADY_RESERVED);
        }

        int amount = trainerProfile.getPricePerSession() * schedules.size();
        LocalDateTime expiresAt = LocalDateTime.now().plus(paymentProperties.orderTtl());
        String orderId = UUID.randomUUID().toString();

        paymentOrderRepository.save(PaymentOrder.ready(
                user,
                trainerProfile,
                orderId,
                amount,
                request.message(),
                request.userInfo().name(),
                request.userInfo().email(),
                request.userInfo().phoneNumber(),
                request.availableScheduleIds(),
                expiresAt));

        return PaymentPrepareResponse.of(orderId, amount);
    }
}
