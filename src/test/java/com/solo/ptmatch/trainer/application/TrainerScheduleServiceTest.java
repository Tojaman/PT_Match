package com.solo.ptmatch.trainer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.TrainerSchedule;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleListRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleUpdateRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleUpdateRequestItem;
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
class TrainerScheduleServiceTest {

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
        TrainerScheduleListRequest request = new TrainerScheduleListRequest(
                List.of(TrainerSchedule.of(start, end))
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByTrainerId(user.getId())).thenReturn(Optional.of(profile));
        when(availableScheduleRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        TrainerScheduleListResponse response = trainerScheduleService.registerTrainerSchedule(email, request);

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
        assertThat(response.shedules()).hasSize(1);
        assertThat(response.shedules().get(0).startTime()).isEqualTo(start);
        assertThat(response.shedules().get(0).endTime()).isEqualTo(end);
    }

    @DisplayName("등록 요청 시 로그인 이메일과 매칭되는 사용자가 없으면 예외를 던진다")
    @Test
    void registerTrainerSchedule_UserNotFound() {
        // given
        String missingEmail = "missing@example.com";
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        TrainerScheduleListRequest request = new TrainerScheduleListRequest(
                List.of(TrainerSchedule.of(start, end))
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
        when(trainerProfileRepository.findByTrainerId(user.getId())).thenReturn(Optional.of(profile));
        when(availableScheduleRepository.findByIdAndTrainerProfileId(100L, profile.getId()))
                .thenReturn(existingSchedule);

        // when
        TrainerScheduleListResponse response = trainerScheduleService.updateTrainerSchedule(email, request);

        // then
        assertThat(existingSchedule.getStartTime()).isEqualTo(updatedStart);
        assertThat(existingSchedule.getEndTime()).isEqualTo(updatedEnd);
        assertThat(response.shedules()).hasSize(1);
        assertThat(response.shedules().get(0).startTime()).isEqualTo(updatedStart);
        assertThat(response.shedules().get(0).endTime()).isEqualTo(updatedEnd);
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
        when(trainerProfileRepository.findByTrainerId(user.getId())).thenReturn(Optional.of(profile));
        when(availableScheduleRepository.findByIdAndTrainerProfileId(100L, profile.getId()))
                .thenReturn(null); // 존재하지 않는 스케줄

        // when & then
        assertThatThrownBy(() -> trainerScheduleService.updateTrainerSchedule(email, request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND.getMessage());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
