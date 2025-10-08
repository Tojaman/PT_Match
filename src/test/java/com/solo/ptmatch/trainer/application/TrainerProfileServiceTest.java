package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.review.domain.Review;
import com.solo.ptmatch.review.infrastructure.ReviewRepository;
import com.solo.ptmatch.trainer.domain.Certification;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.CertificationRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.TrainerCertificationRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerProfileServiceTest {

    @InjectMocks
    private TrainerProfileService trainerProfileService;

    @Mock
    private TrainerProfileRepository trainerProfileRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AvailableScheduleRepository availableScheduleRepository; // 사용되지는 않지만 의존성으로 존재

    @DisplayName("트레이너 목록을 조건에 맞게 조회하고 DTO 리스트로 변환하여 반환한다.")
    @Test
    void getTrainerSummaries_Success() {
        // given
        // 1. 검색 조건 및 페이징 정보 생성
        TrainerSearchRequest request = new TrainerSearchRequest("다이어트", "서울", "averageRating_desc", 0, 10);
        Sort sort = Sort.by(Sort.Direction.DESC, "averageRating");
        Pageable pageable = PageRequest.of(0, 10, sort);

        // 2. Mock 데이터 생성 (Repository가 반환할 데이터)
        User user = User.create("trainer@test.com", "password", "김전문", Role.TRAINER);
        TrainerProfile profile = TrainerProfile.create(user, "bio", 5, Specialty.DIET, "서울", "url");

        List<TrainerProfile> profiles = List.of(profile);
        Page<TrainerProfile> mockPage = new PageImpl<>(profiles, pageable, profiles.size());

        // 3. Mock Repository 설정: searchBySpecialtyAndGymAddress가 호출되면 mockPage를 반환하도록 설정
        when(trainerProfileRepository.searchBySpecialtyAndGymAddress(
                eq(Specialty.DIET),
                eq("서울"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // when
        // 4. 테스트 대상 메서드 호출
        Page<TrainerSummaryResponse> result = trainerProfileService.getTrainerSummaries(request);

        // then
        // 5. 결과 검증
        assertThat(result).hasSize(1);
        TrainerSummaryResponse summary = result.getContent().get(0);
        assertThat(summary.name()).isEqualTo("김전문");
        assertThat(summary.specialties()).isEqualTo(Specialty.DIET.name());
        assertThat(summary.gymAddress()).isEqualTo("서울");

        // 6. Repository 메서드가 정확한 인자와 함께 호출되었는지 검증
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(trainerProfileRepository, times(1)).searchBySpecialtyAndGymAddress(
                eq(Specialty.DIET),
                eq("서울"),
                pageableCaptor.capture()
        );

        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
        assertThat(capturedPageable.getPageSize()).isEqualTo(10);
        assertThat(capturedPageable.getSort()).isEqualTo(sort);
    }

    @DisplayName("트레이너 상세 정보 조회 시, 프로필/자격증/최신 리뷰 5개를 조합하여 반환한다")
    @Test
    void getTrainerDetail_Success() throws Exception {
        // given
        long trainerId = 1L;

        // 1. 테스트용 실제 객체 생성
        User user = User.create("trainer@test.com", "password", "김상세", Role.TRAINER);
        TrainerProfile profile = TrainerProfile.create(user, "상세한 자기소개", 5, Specialty.DIET, "서울 강남", "profile.jpg");

        // private final id 필드에 값을 세팅하기 위해 리플렉션 사용
        Field idField = TrainerProfile.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(profile, trainerId);

        Review review = Review.create(user, profile, 5, "정말 최고의 코칭이었습니다!");
        Certification certification = Certification.create(profile, "NSCA-CPT", "NSCA", LocalDate.now());

        List<Review> reviews = List.of(review);
        List<Certification> certifications = List.of(certification);

        // 2. Mock Repository 설정
        when(trainerProfileRepository.findByUserId(trainerId)).thenReturn(Optional.of(profile));
        when(certificationRepository.findAllByTrainerProfileId(profile.getId())).thenReturn(certifications);

        // when
        TrainerDetailResponse result = trainerProfileService.getTrainerDetail(trainerId);

        // then
        // 3. 반환된 DTO의 값 검증
        assertThat(result).isNotNull();
        assertThat(result.trainerId()).isEqualTo(trainerId);
        assertThat(result.name()).isEqualTo("김상세");
        assertThat(result.bio()).isEqualTo("상세한 자기소개");
        assertThat(result.certifications()).hasSize(1);
        assertThat(result.certifications().get(0).name()).isEqualTo("NSCA-CPT");

        // 4. Repository 메서드 호출 여부 검증
        verify(trainerProfileRepository, times(1)).findByUserId(trainerId);
        verify(certificationRepository, times(1)).findAllByTrainerProfileId(profile.getId());
    }

    @DisplayName("존재하지 않는 트레이너 ID로 상세 조회 시 예외를 발생시킨다")
    @Test
    void getTrainerDetail_ProfileNotFound() {
        // given
        long nonExistentTrainerId = 999L;
        when(trainerProfileRepository.findByUserId(nonExistentTrainerId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> trainerProfileService.getTrainerDetail(nonExistentTrainerId))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("트레이너 프로필을 찾을 수 없습니다.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TRAINER_PROFILE_NOT_FOUND);

        verify(certificationRepository, never()).findAllByTrainerProfileId(any());
    }

    @DisplayName("트레이너 프로필과 자격증을 성공적으로 등록한다")
    @Test
    void registerTrainerProfile_Success() {
        // given
        String email = "new.trainer@ptmatch.com";
        User user = User.create(email, "password", "새트레이너", Role.TRAINER);
        TrainerProfileUpsertRequest request = new TrainerProfileUpsertRequest(
                "새로운 자기소개", 1, Specialty.DIET, "서울", "url",
                List.of(new TrainerCertificationRequest("자격증1", "발급기관1", LocalDate.now()))
        );

        TrainerProfile profile = request.toEntity(user);
        TrainerProfileUpsertResponse response = TrainerProfileUpsertResponse.from(profile);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(any())).thenReturn(Optional.empty());
        when(trainerProfileRepository.save(any(TrainerProfile.class))).thenReturn(profile);

        // when
        TrainerProfileUpsertResponse result = trainerProfileService.registerTrainerProfile(email, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.bio()).isEqualTo("새로운 자기소개");

        verify(userRepository, times(1)).findByEmail(email);
        verify(trainerProfileRepository, times(1)).findByUserId(user.getId());
        verify(trainerProfileRepository, times(1)).save(any(TrainerProfile.class));
        verify(certificationRepository, times(1)).saveAll(any());
    }

    @DisplayName("프로필 등록 시 사용자를 찾을 수 없으면 예외를 발생시킨다")
    @Test
    void registerTrainerProfile_UserNotFound() {
        // given
        String email = "nonexistent@ptmatch.com";
        TrainerProfileUpsertRequest request = new TrainerProfileUpsertRequest(
                "자기소개", 1, Specialty.DIET, "서울", "url", List.of()
        );
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> trainerProfileService.registerTrainerProfile(email, request))
                .isInstanceOf(GlobalException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(trainerProfileRepository, never()).save(any());
    }

    @DisplayName("프로필 등록 시 이미 프로필이 존재하면 예외를 발생시킨다")
    @Test
    void registerTrainerProfile_ProfileAlreadyExists() {
        // given
        String email = "new.trainer@ptmatch.com";
        User user = User.create(email, "password", "새트레이너", Role.TRAINER);
        TrainerProfileUpsertRequest request = new TrainerProfileUpsertRequest(
                "자기소개", 1, Specialty.DIET, "서울", "url", List.of()
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(mock(TrainerProfile.class)));

        // when & then
        assertThatThrownBy(() -> trainerProfileService.registerTrainerProfile(email, request))
                .isInstanceOf(GlobalException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);

        verify(trainerProfileRepository, never()).save(any());
    }

    @DisplayName("기존 트레이너 프로필 정보를 성공적으로 수정한다")
    @Test
    void updateTrainerProfile_Success() {
        // given
        String email = "trainer@ptmatch.com";
        User user = User.create(email, "password", "트레이너", Role.TRAINER);
        TrainerProfile existingProfile = mock(TrainerProfile.class);

        TrainerProfileUpsertRequest request = new TrainerProfileUpsertRequest(
                "수정된 자기소개", 10, Specialty.DIET, "서울 용산구", "new_url", null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(existingProfile));

        // when
        trainerProfileService.updateTrainerProfile(email, request);

        // then
        // 1. profile 객체의 updateProfile 메서드가 정확한 인자와 함께 호출되었는지 검증 (가장 중요)
        verify(existingProfile, times(1)).updateProfile(
                request.bio(),
                request.careerYears(),
                request.specialties(),
                request.gymAddress(),
                request.profileImageUrl()
        );

        // 2. 불필요한 save 호출이 없는지 검증 (더티 체킹으로 업데이트되므로)
        verify(trainerProfileRepository, never()).save(any(TrainerProfile.class));
    }

    @DisplayName("프로필 수정 시 사용자를 찾을 수 없으면 예외를 발생시킨다")
    @Test
    void updateTrainerProfile_UserNotFound() {
        // given
        String email = "nonexistent@ptmatch.com";
        TrainerProfileUpsertRequest request = new TrainerProfileUpsertRequest(
                "자기소개", 1, Specialty.DIET, "서울", "url", null
        );
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> trainerProfileService.updateTrainerProfile(email, request))
                .isInstanceOf(GlobalException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @DisplayName("수정할 프로필이 존재하지 않으면 예외를 발생시킨다")
    @Test
    void updateTrainerProfile_ProfileNotFound() {
        // given
        String email = "trainer@ptmatch.com";
        User user = User.create(email, "password", "트레이너", Role.TRAINER);
        TrainerProfileUpsertRequest request = new TrainerProfileUpsertRequest(
                "자기소개", 1, Specialty.DIET, "서울", "url", null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trainerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> trainerProfileService.updateTrainerProfile(email, request))
                .isInstanceOf(GlobalException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TRAINER_PROFILE_NOT_FOUND);
    }
}