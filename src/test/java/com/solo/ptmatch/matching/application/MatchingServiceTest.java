package com.solo.ptmatch.matching.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingSchedule;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import com.solo.ptmatch.matching.domain.SessionStatus;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.request.UserInfo;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingScheduleSummary;
import com.solo.ptmatch.matching.presentation.response.MatchingSentSummaryResponse;
import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.product.domain.ProductCategory;
import com.solo.ptmatch.product.infrastructure.ProductRepository;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.lang.reflect.Field;
import java.math.BigDecimal;
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
class MatchingServiceTest {

    @InjectMocks
    private MatchingService matchingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AvailableScheduleRepository availableScheduleRepository;

    @Mock
    private MatchingRepository matchingRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TrainerProfileRepository trainerProfileRepository;

    @DisplayName("요청 정보를 이용해 매칭을 생성하고 응답을 반환한다")
    @Test
    void requestMatching_Success() throws Exception {
        // given
        String userEmail = "applicant@example.com";
        User user = User.create(userEmail, "encoded", "김회원", Role.USER);
        setField(user, "id", 1L);

        TrainerProfile profile = TrainerProfile.create(user, "소개", 5, Specialty.DIET, "서울", "profile.jpg");
        setField(profile, "id", 10L);

        Product product = Product.create(profile, "10회 패키지", "설명", ProductCategory.WEIGHT_LOSS,
                BigDecimal.valueOf(100000), 10);
        setField(product, "id", 100L);

        LocalDateTime firstStart = LocalDateTime.of(2025, 2, 1, 9, 0);
        LocalDateTime firstEnd = firstStart.plusHours(1);
        AvailableSchedule firstSchedule = AvailableSchedule.create(profile, firstStart, firstEnd);
        setField(firstSchedule, "id", 1000L);

        LocalDateTime secondStart = LocalDateTime.of(2025, 2, 3, 9, 0);
        LocalDateTime secondEnd = secondStart.plusHours(1);
        AvailableSchedule secondSchedule = AvailableSchedule.create(profile, secondStart, secondEnd);
        setField(secondSchedule, "id", 1001L);

        UserInfo userInfo = new UserInfo("변경된이름", "custom@example.com", "010-1234-5678");
        MatchingRequestCreateRequest request = new MatchingRequestCreateRequest(
                product.getId(),
                profile.getId(),
                List.of(firstSchedule.getId(), secondSchedule.getId()),
                "주 2회 진행 희망",
                userInfo
        );

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(availableScheduleRepository.findAllByIdIn(request.availableScheduleIds()))
                .thenReturn(List.of(firstSchedule, secondSchedule));
        when(matchingRepository.save(any(Matching.class))).thenAnswer(invocation -> {
            Matching saved = invocation.getArgument(0);
            setField(saved, "id", 2000L);
            long scheduleId = 3000L;
            for (MatchingSchedule matchingSchedule : saved.getSchedules()) {
                setField(matchingSchedule, "id", scheduleId++);
            }
            return saved;
        });

        // when
        MatchingRequestCreateResponse response = matchingService.requestMatching(userEmail, request);

        // then
        assertThat(response.matchingId()).isEqualTo(2000L);
        assertThat(response.status()).isEqualTo(MatchingStatus.PENDING);
        assertThat(response.schedules()).hasSize(2);
        assertThat(response.schedules()).extracting(MatchingScheduleSummary::matchingScheduleId)
                .containsExactly(3000L, 3001L);
        assertThat(response.schedules()).extracting(MatchingScheduleSummary::availableScheduleId)
                .containsExactly(firstSchedule.getId(), secondSchedule.getId());
        assertThat(response.schedules().get(0).startTime()).isEqualTo(firstStart);
        assertThat(response.schedules().get(0).endTime()).isEqualTo(firstEnd);
        assertThat(response.schedules().get(1).startTime()).isEqualTo(secondStart);
        assertThat(response.schedules().get(1).endTime()).isEqualTo(secondEnd);
        assertThat(response.schedules()).allMatch(summary -> summary.sessionStatus() == SessionStatus.SCHEDULED);
        assertThat(response.matchingUserInfo().getName()).isEqualTo("변경된이름");
        assertThat(response.matchingUserInfo().getEmail()).isEqualTo("custom@example.com");
        assertThat(response.matchingUserInfo().getPhone()).isEqualTo("010-1234-5678");
        assertThat(firstSchedule.getReservationStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(secondSchedule.getReservationStatus()).isEqualTo(ReservationStatus.PENDING);

        ArgumentCaptor<Matching> matchingCaptor = ArgumentCaptor.forClass(Matching.class);
        verify(matchingRepository).save(matchingCaptor.capture());
        Matching captured = matchingCaptor.getValue();
        MatchingUserInfo contact = captured.getMatchingUserInfo();
        assertThat(contact.getName()).isEqualTo("변경된이름");
        assertThat(contact.getEmail()).isEqualTo("custom@example.com");
        assertThat(contact.getPhone()).isEqualTo("010-1234-5678");
    }

    @DisplayName("조회된 스케줄이 예약 불가 상태면 예외를 던진다")
    @Test
    void requestMatching_FailsWhenScheduleReserved() throws Exception {
        // given
        String userEmail = "applicant@example.com";
        User user = User.create(userEmail, "encoded", "김회원", Role.USER);
        setField(user, "id", 1L);

        TrainerProfile profile = TrainerProfile.create(user, "소개", 5, Specialty.DIET, "서울", "profile.jpg");
        setField(profile, "id", 10L);

        Product product = Product.create(profile, "10회 패키지", "설명", ProductCategory.WEIGHT_LOSS,
                BigDecimal.valueOf(100000), 10);
        setField(product, "id", 100L);

        LocalDateTime start = LocalDateTime.of(2025, 2, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        AvailableSchedule schedule = AvailableSchedule.create(profile, start, end);
        setField(schedule, "id", 1000L);
        setField(schedule, "reservationStatus", ReservationStatus.CONFIRMED);

        MatchingRequestCreateRequest request = new MatchingRequestCreateRequest(
                product.getId(),
                profile.getId(),
                List.of(schedule.getId()),
                "주 2회 진행 희망",
                new UserInfo("변경된이름", "custom@example.com", "010-1234-5678")
        );

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(availableScheduleRepository.findAllByIdIn(request.availableScheduleIds()))
                .thenReturn(List.of(schedule));

        // when & then
        assertThatThrownBy(() -> matchingService.requestMatching(userEmail, request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining(ErrorCode.SCHEDULE_ALREADY_RESERVED.getMessage());
    }

    @DisplayName("스케줄이 존재하지 않으면 예외를 던진다")
    @Test
    void requestMatching_FailsWhenScheduleMissing() {
        // given
        String userEmail = "applicant@example.com";
        User user = User.create(userEmail, "encoded", "김회원", Role.USER);

        TrainerProfile profile = TrainerProfile.create(user, "소개", 5, Specialty.DIET, "서울", "profile.jpg");
        Product product = Product.create(profile, "10회 패키지", "설명", ProductCategory.WEIGHT_LOSS,
                BigDecimal.valueOf(100000), 10);

        MatchingRequestCreateRequest request = new MatchingRequestCreateRequest(
                100L,
                10L,
                List.of(1000L),
                "주 2회 진행 희망",
                new UserInfo("변경된이름", "custom@example.com", "010-1234-5678")
        );

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(availableScheduleRepository.findAllByIdIn(request.availableScheduleIds()))
                .thenReturn(List.of((AvailableSchedule) null));

        // when & then
        assertThatThrownBy(() -> matchingService.requestMatching(userEmail, request))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND.getMessage());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @DisplayName("사용자가 보낸 매칭 목록을 DTO 리스트로 변환하여 반환한다.")
    @Test
    void getSentMatchings_Success() throws Exception {
        // given
        String userEmail = "applicant@example.com";
        User user = User.create(userEmail, "password", "김회원", Role.USER);
        setField(user, "id", 1L);

        User trainer = User.create("trainer@example.com", "password", "김트레이너", Role.TRAINER);
        setField(trainer, "id", 2L);

        TrainerProfile trainerProfile = TrainerProfile.create(trainer, "bio", 5, Specialty.DIET, "gym address", "img");
        setField(trainerProfile, "id", 10L);

        Product product1 = Product.create(trainerProfile, "Product 1", "desc", ProductCategory.WEIGHT_LOSS, BigDecimal.TEN, 10);
        setField(product1, "id", 100L);

        Product product2 = Product.create(trainerProfile, "Product 2", "desc", ProductCategory.MUSCLE_GAIN, BigDecimal.ONE, 1);
        setField(product2, "id", 101L);

        Matching matching1 = createMockMatching(200L, user, trainerProfile, product1);
        Matching matching2 = createMockMatching(201L, user, trainerProfile, product2);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(matchingRepository.findAllByUserIdWithDetails(user.getId())).thenReturn(List.of(matching1, matching2));

        // when
        List<MatchingSentSummaryResponse> responses = matchingService.getSentMatchings(userEmail);

        // then
        assertThat(responses).hasSize(2);

        MatchingSentSummaryResponse response1 = responses.get(0);
        assertThat(response1.matchingId()).isEqualTo(200L);
        assertThat(response1.matchingProductInfo().productId()).isEqualTo(100L);
        assertThat(response1.matchingProductInfo().productTitle()).isEqualTo("Product 1");
        assertThat(response1.matchingTrainerInfo().trainerId()).isEqualTo(2L);
        assertThat(response1.matchingTrainerInfo().trainerName()).isEqualTo("김트레이너");

        MatchingSentSummaryResponse response2 = responses.get(1);
        assertThat(response2.matchingId()).isEqualTo(201L);
        assertThat(response2.matchingProductInfo().productId()).isEqualTo(101L);
        assertThat(response2.matchingTrainerInfo().trainerName()).isEqualTo("김트레이너");

        verify(userRepository).findByEmail(userEmail);
        verify(matchingRepository).findAllByUserIdWithDetails(user.getId());
    }

    @DisplayName("보낸 매칭 목록 조회 시 사용자를 찾지 못하면 예외를 던진다.")
    @Test
    void getSentMatchings_FailsWhenUserNotFound() {
        // given
        String userEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> matchingService.getSentMatchings(userEmail))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    private Matching createMockMatching(Long matchingId, User user, TrainerProfile trainerProfile, Product product) throws Exception {
        Matching matching = Matching.create(user, trainerProfile, product, "message", MatchingUserInfo.of("name", "email", "phone"));
        setField(matching, "id", matchingId);
        return matching;
    }
}
