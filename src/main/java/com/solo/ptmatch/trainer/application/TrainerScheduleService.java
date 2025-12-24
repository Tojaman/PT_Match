package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.*;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
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
    public List<TrainerScheduleListResponse> registerTrainerSchedule(String email, TrainerScheduleCreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        // 스케줄 저장
        List<AvailableSchedule> availableSchedules = new ArrayList<>();

        // 요청 데이터 내 중복 제거
        for (TrainerScheduleRequest req : request.schedules()) {
            // DB 중복 검사
            if (availableScheduleRepository.countOverlappingSchedule(profile.getId(), req.startTime(), req.endTime()) > 0) {
                throw GlobalException.of(ErrorCode.SCHEDULE_DUPLICATED);
            }
            availableSchedules.add(AvailableSchedule.create(profile, req.startTime(), req.endTime()));
        }
        availableScheduleRepository.saveAll(availableSchedules);

        return availableSchedules.stream()
                .map(TrainerScheduleListResponse::from)
                .toList();
    }

    @Transactional
    public void deleteTrainerSchedule(
            String email,
            TrainerScheduleDeleteRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        List<AvailableSchedule> schedules = availableScheduleRepository.findAllByIdInAndTrainerProfileId(request.scheduleIds(), profile.getId());
        // 요청한 스케줄 개수와 조회된 스케줄 개수가 다른 경우 -> 일부 스케줄이 트레이너 스케줄이 아니거나 존재하지 않는 경우
        if (schedules.size() != request.scheduleIds().size()) {
            throw GlobalException.of(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND);
        }
        for (AvailableSchedule schedule : schedules) {
            if (schedule.getReservationStatus() == ReservationStatus.PENDING || schedule.getReservationStatus() == ReservationStatus.CONFIRMED) {
                throw GlobalException.of(ErrorCode.CANNOT_DELETE_RESERVED_SCHEDULE);
            }
        }
        availableScheduleRepository.deleteAll(schedules);
    }

    @Transactional(readOnly = true)
    public List<TrainerScheduleListResponse> getTrainerSchedules(Long trainerId) {

        if (!trainerProfileRepository.existsById(trainerId)) {
            throw GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND);
        }

        return availableScheduleRepository.findAllByTrainerProfileId(trainerId).stream()
                .map(TrainerScheduleListResponse::from)
                .toList();
    }
}
