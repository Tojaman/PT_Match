package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
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
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerProfileServiceTest {

    @InjectMocks
    private TrainerProfileService trainerProfileService;

    @Mock
    private TrainerProfileRepository trainerProfileRepository;

    @DisplayName("트레이너 목록을 조건에 맞게 조회하고 DTO 리스트로 변환하여 반환한다.")
    @Test
    void getTrainerSummaries_Success() {
        // given
        // 1. 검색 조건 및 페이징 정보 생성
        TrainerSearchRequest request = new TrainerSearchRequest("DIET", "서울", "averageRating_desc", 0, 10);
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
        List<TrainerSummaryResponse> result = trainerProfileService.getTrainerSummaries(request);

        // then
        // 5. 결과 검증
        assertThat(result).hasSize(1);
        TrainerSummaryResponse summary = result.get(0);
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
}