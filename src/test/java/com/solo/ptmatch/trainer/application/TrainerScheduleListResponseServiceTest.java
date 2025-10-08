package com.solo.ptmatch.trainer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleDeleteRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleCreateRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleUpdateRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerScheduleListResponseServiceTest {

    @InjectMocks
    private TrainerScheduleService trainerScheduleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainerProfileRepository trainerProfileRepository;

    @Mock
    private AvailableScheduleRepository availableScheduleRepository;

    @DisplayName("트레이너 스케줄 등록 시 요청 슬롯을 저장하고 응답 DTO로 반환한다")
    @Test
    void registerTrainerSchedule_Success() throws Exception {
        // given
        String email = "trainer@example.com";
        User user = User.create(email, "encoded", "홍트레이너", Role.TRAINER);
        setField(user, "id", 1L);

        TrainerProfile profile = TrainerProfile.create(
                user,
                "소개",
                5,
                Specialty.DIET,
                "서울",
                "image.jpg"
        );
        setField(profile, "id", 10L);

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        TrainerScheduleCreateRequest request = new TrainerScheduleCreateRequest(
                List.of(new TrainerScheduleCreateRequest.ScheduleRequest(start, end))
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(availableScheduleRepository.saveAll(any())).thenAnswer(invocation -> {
            List<AvailableSchedule> schedules = invocation.getArgument(0);
            long id = 1L;
            for (AvailableSchedule schedule : schedules) {
                setField(schedule, "id", id++);
            }
            return schedules;
        });

        // when
        List<TrainerScheduleListResponse> response = trainerScheduleService.registerTrainerSchedule(email, request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AvailableSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(availableScheduleRepository).saveAll(captor.capture());
        List<AvailableSchedule> savedSchedules = captor.getValue();
        assertThat(savedSchedules).hasSize(1);
        assertThat(savedSchedules.get(0).getStartTime()).isEqualTo(start);
        assertThat(savedSchedules.get(0).getEndTime()).isEqualTo(end);
        assertThat(savedSchedules.get(0).getTrainerProfile()).isEqualTo(profile);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).startTime()).isEqualTo(start);
        assertThat(response.get(0).endTime()).isEqualTo(end);
    }

    @DisplayName("등록 요청 시 로그인 이메일과 매칭되는 사용자가 없으면 예외를 던진다")
    @Test
    void registerTrainerSchedule_UserNotFound() {
        // given
        String missingEmail = "missing@example.com";
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        TrainerScheduleCreateRequest request = new TrainerScheduleCreateRequest(
                List.of(new TrainerScheduleCreateRequest.ScheduleRequest(start, end))
        );

        when(userRepository.findByEmail(missingEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> trainerScheduleService.registerTrainerSchedule(missingEmail, request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.name());
    }

    @DisplayName("트레이너 스케줄 수정 시 각 슬롯을 업데이트하고 응답 DTO로 반환한다")
    @Test
    void updateTrainerSchedule_Success() throws Exception {
        // given
        String email = "trainer@example.com";
        User user = User.create(email, "encoded", "홍트레이너", Role.TRAINER);
        setField(user, "id", 1L);

        TrainerProfile profile = TrainerProfile.create(
                user,
                "소개",
                5,
                Specialty.DIET,
                "서울",
                "image.jpg"
        );
        setField(profile, "id", 10L);

        LocalDateTime originalStart = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime originalEnd = originalStart.plusHours(1);
        AvailableSchedule existingSchedule = AvailableSchedule.create(profile, originalStart, originalEnd);
        setField(existingSchedule, "id", 100L);

        LocalDateTime updatedStart = LocalDateTime.of(2025, 1, 2, 10, 0);
        LocalDateTime updatedEnd = updatedStart.plusHours(1);
        TrainerScheduleUpdateRequest request = new TrainerScheduleUpdateRequest(
                List.of(new TrainerScheduleUpdateRequestItem(100L, updatedStart, updatedEnd))
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(availableScheduleRepository.findByIdAndTrainerProfileId(100L, profile.getId()))
                .thenReturn(existingSchedule);

        // when
        List<TrainerScheduleListResponse> response = trainerScheduleService.updateTrainerSchedule(email, request);

        // then
        assertThat(existingSchedule.getStartTime()).isEqualTo(updatedStart);
        assertThat(existingSchedule.getEndTime()).isEqualTo(updatedEnd);
        assertThat(response).hasSize(1);
        assertThat(response.get(0).startTime()).isEqualTo(updatedStart);
        assertThat(response.get(0).endTime()).isEqualTo(updatedEnd);
    }

    @DisplayName("트레이너 스케줄 수정 시 존재하지 않는 슬롯이면 예외를 던진다")
    @Test
    void updateTrainerSchedule_ScheduleNotFound() throws Exception {
        // given
        String email = "trainer@example.com";
        User user = User.create(email, "encoded", "홍트레이너", Role.TRAINER);
        setField(user, "id", 1L);

        TrainerProfile profile = TrainerProfile.create(
                user,
                "소개",
                5,
                Specialty.DIET,
                "서울",
                "image.jpg"
        );
        setField(profile, "id", 10L);

        LocalDateTime updatedStart = LocalDateTime.of(2025, 1, 2, 10, 0);
        LocalDateTime updatedEnd = updatedStart.plusHours(1);
        TrainerScheduleUpdateRequest request = new TrainerScheduleUpdateRequest(
                List.of(new TrainerScheduleUpdateRequestItem(100L, updatedStart, updatedEnd))
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(availableScheduleRepository.findByIdAndTrainerProfileId(100L, profile.getId()))
                .thenReturn(null); // 존재하지 않는 스케줄

        // when & then
        assertThatThrownBy(() -> trainerScheduleService.updateTrainerSchedule(email, request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND.getMessage());
    }

    @DisplayName("트레이너 스케줄 삭제 시 모든 슬롯이 본인 소유이면서 예약 가능 상태면 삭제한다")
    @Test
    void deleteTrainerSchedule_Success() throws Exception {
        // given
        String email = "trainer@example.com";
        User user = User.create(email, "encoded", "홍트레이너", Role.TRAINER);
        setField(user, "id", 1L);

        TrainerProfile profile = TrainerProfile.create(
                user,
                "소개",
                5,
                Specialty.DIET,
                "서울",
                "image.jpg"
        );
        setField(profile, "id", 10L);

        AvailableSchedule schedule = AvailableSchedule.create(
                profile,
                LocalDateTime.of(2025, 1, 3, 9, 0),
                LocalDateTime.of(2025, 1, 3, 10, 0)
        );
        setField(schedule, "id", 200L);

        TrainerScheduleDeleteRequest request = new TrainerScheduleDeleteRequest(List.of(200L));
        List<AvailableSchedule> schedules = List.of(schedule);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(availableScheduleRepository.findAllByIdInAndTrainerProfileId(request.scheduleIds(), profile.getId()))
                .thenReturn(schedules);

        // when
        trainerScheduleService.deleteTrainerSchedule(email, request);

        // then
        verify(availableScheduleRepository).deleteAll(schedules);
    }

    @DisplayName("트레이너 스케줄 삭제 시 일부 슬롯이 존재하지 않으면 예외를 던진다")
    @Test
    void deleteTrainerSchedule_ScheduleNotFound() throws Exception {
        // given
        String email = "trainer@example.com";
        User user = User.create(email, "encoded", "홍트레이너", Role.TRAINER);
        setField(user, "id", 1L);

        TrainerProfile profile = TrainerProfile.create(
                user,
                "소개",
                5,
                Specialty.DIET,
                "서울",
                "image.jpg"
        );
        setField(profile, "id", 10L);

        TrainerScheduleDeleteRequest request = new TrainerScheduleDeleteRequest(List.of(201L, 202L));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(availableScheduleRepository.findAllByIdInAndTrainerProfileId(request.scheduleIds(), profile.getId()))
                .thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> trainerScheduleService.deleteTrainerSchedule(email, request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND.getMessage());
    }

    @DisplayName("트레이너 스케줄 삭제 시 예약 대기 또는 확정 상태 슬롯이 포함되면 예외를 던진다")
    @Test
    void deleteTrainerSchedule_ReservedSchedule() throws Exception {
        // given
        String email = "trainer@example.com";
        User user = User.create(email, "encoded", "홍트레이너", Role.TRAINER);
        setField(user, "id", 1L);

        TrainerProfile profile = TrainerProfile.create(
                user,
                "소개",
                5,
                Specialty.DIET,
                "서울",
                "image.jpg"
        );
        setField(profile, "id", 10L);

        AvailableSchedule reservedSchedule = AvailableSchedule.create(
                profile,
                LocalDateTime.of(2025, 1, 4, 9, 0),
                LocalDateTime.of(2025, 1, 4, 10, 0)
        );
        setField(reservedSchedule, "id", 300L);
        setField(reservedSchedule, "reservationStatus", ReservationStatus.CONFIRMED);

        TrainerScheduleDeleteRequest request = new TrainerScheduleDeleteRequest(List.of(300L));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(availableScheduleRepository.findAllByIdInAndTrainerProfileId(request.scheduleIds(), profile.getId()))
                .thenReturn(List.of(reservedSchedule));

        // when & then
        assertThatThrownBy(() -> trainerScheduleService.deleteTrainerSchedule(email, request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining(ErrorCode.CANNOT_DELETE_RESERVED_SCHEDULE.getMessage());
    }

    @DisplayName("트레이너 스케줄 조회 시 존재하는 프로필의 슬롯 목록을 반환한다")
    @Test
    void getTrainerSchedules_Success() throws Exception {
        // given
        Long trainerId = 42L;

        when(trainerProfileRepository.existsById(trainerId)).thenReturn(true);

        User user = User.create("trainer@example.com", "encoded", "홍트레이너", Role.TRAINER);
        setField(user, "id", 1L);
        TrainerProfile profile = TrainerProfile.create(
                user,
                "소개",
                5,
                Specialty.DIET,
                "서울",
                "image.jpg"
        );
        setField(profile, "id", trainerId);

        LocalDateTime start = LocalDateTime.of(2025, 1, 6, 9, 0);
        LocalDateTime end = start.plusHours(1);
        AvailableSchedule schedule = AvailableSchedule.create(profile, start, end);

        when(availableScheduleRepository.findAllByTrainerProfileId(trainerId))
                .thenReturn(List.of(schedule));

        // when
        List<TrainerScheduleListResponse> response = trainerScheduleService.getTrainerSchedules(trainerId);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).startTime()).isEqualTo(start);
        assertThat(response.get(0).endTime()).isEqualTo(end);
    }

    @DisplayName("트레이너 스케줄 조회 시 프로필이 없으면 예외를 던진다")
    @Test
    void getTrainerSchedules_ProfileNotFound() {
        // given
        Long trainerId = 99L;
        when(trainerProfileRepository.existsById(trainerId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> trainerScheduleService.getTrainerSchedules(trainerId))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining(ErrorCode.TRAINER_PROFILE_NOT_FOUND.getMessage());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
