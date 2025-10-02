package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.*;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.server.ui.OneTimeTokenSubmitPageGeneratingWebFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TrainerScheduleService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final AvailableScheduleRepository availableScheduleRepository;

    @Transactional
    public TrainerScheduleListResponse registerTrainerSchedule(
            String email,
            TrainerScheduleListRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByTrainerId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        // 스케줄 저장
        List<AvailableSchedule> availableSchedules = request.schedules().stream()
                .map(scheduleRequest -> AvailableSchedule.create(
                        profile,
                        scheduleRequest.startTime(),
                        scheduleRequest.endTime()
                ))
                .toList();
        availableScheduleRepository.saveAll(availableSchedules);

        // 저장된 스케줄을 TrainerScheduleListResponse로 변환
        List<TrainerSchedule> schedules = availableSchedules.stream()
                .map(
                        schedule -> TrainerSchedule.of(
                                schedule.getStartTime(),
                                schedule.getEndTime()
                        ))
                .toList();
        return TrainerScheduleListResponse.from(schedules);
    }

    @Transactional
    public TrainerScheduleListResponse updateTrainerSchedule(
            String email,
            TrainerScheduleUpdateRequest request
    ) {

    }

//    @Transactional
//    public TrainerScheduleListResponse deleteTrainerSchedule(
//            String email,
//            TrainerScheduleDeleteRequest request
//    ) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));
//
//        TrainerProfile profile = trainerProfileRepository.findByTrainerId(user.getId())
//                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));
//
//
//    }
}
