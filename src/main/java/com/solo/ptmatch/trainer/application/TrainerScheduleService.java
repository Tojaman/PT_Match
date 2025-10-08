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
    public List<TrainerScheduleListResponse> registerTrainerSchedule(
            String email,
            TrainerScheduleCreateRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByUserId(user.getId())
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
        return availableSchedules.stream()
                .map(TrainerScheduleListResponse::from)
                .toList();
    }

    @Transactional
    public List<TrainerScheduleListResponse> updateTrainerSchedule(
            String email,
            TrainerScheduleUpdateRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        // 업데이트 대상 스케줄 조회 및 업데이트
        List<TrainerScheduleListResponse> updatedSchedules = new ArrayList<>();
        for (TrainerScheduleRequest req : request.schedules()) {
            AvailableSchedule schedule = availableScheduleRepository.findByIdAndTrainerProfileId(
                    req.scheduleId(),
                    profile.getId());
            if (schedule == null) {
                throw GlobalException.of(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND);
            }
            schedule.update(req.startTime(), req.endTime());
            updatedSchedules.add(TrainerScheduleListResponse.from(schedule));
        }
        return updatedSchedules;
    }

    @Transactional
    public void deleteTrainerSchedule(
            String email,
            TrainerScheduleDeleteRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        /*
        1. 스케줄 조회
        2. 해당 스케줄이 트레이너의 스케줄인지 확인
        3. 해당 스케줄이 예약된 상태인지 확인
        4. 2, 3번 조건 모두 만족하는지 확인 -> 만족한다면 삭제 아니면 예외 처리
         */
        List<AvailableSchedule> schedules = availableScheduleRepository.findAllByIdInAndTrainerProfileId(request.scheduleIds(), profile.getId());
        // 요청한 스케줄 개수와 조회된 스케줄 개수가 다른 경우 -> 일부 스케줄이 트레이너 스케줄이 아니거나 존재하지 않는 경우
        if (schedules.size() != request.scheduleIds().size()) {
            throw GlobalException.of(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND);
        }
        for (AvailableSchedule schedule : schedules) {
            // 예약 대기 중 또는 예약 확정 상태인 경우 예외 처리
            if (schedule.getReservationStatus() == ReservationStatus.PENDING || schedule.getReservationStatus() == ReservationStatus.CONFIRMED) {
                throw GlobalException.of(ErrorCode.CANNOT_DELETE_RESERVED_SCHEDULE);
            }
        }
        availableScheduleRepository.deleteAll(schedules);
    }

    @Transactional(readOnly = true)
    public List<TrainerScheduleListResponse> getTrainerSchedules(Long trainerId) {

        /*
        1. 트레이너 프로필로 스케줄 전체 조회
        2. 스케줄 반환 - 존재하지 않는다면 빈 리스트 반환
         */
        if (!trainerProfileRepository.existsById(trainerId)) {
            throw GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND);
        }

        return availableScheduleRepository.findAllByTrainerProfileId(trainerId).stream()
                .map(TrainerScheduleListResponse::from)
                .toList();
    }
}
