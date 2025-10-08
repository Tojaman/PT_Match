package com.solo.ptmatch.trainer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solo.ptmatch.trainer.domain.TrainerFollow;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerFollowRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.TrainerFollowToggleResponse;
import com.solo.ptmatch.trainer.presentation.response.FollowedTrainerSummaryResponse;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TrainerFollowServiceTest {

    private static final String MEMBER_EMAIL = "member@test.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainerProfileRepository trainerProfileRepository;

    @Mock
    private TrainerFollowRepository trainerFollowRepository;

    @InjectMocks
    private TrainerFollowService trainerFollowService;

    private User member;
    private User trainer;
    private TrainerProfile trainerProfile;

    @BeforeEach
    void setUpEntities() {
        member = User.create(MEMBER_EMAIL, "encoded", "회원", Role.USER);
        ReflectionTestUtils.setField(member, "id", 1L);

        trainer = User.create("trainer@test.com", "encoded", "트레이너", Role.TRAINER);
        ReflectionTestUtils.setField(trainer, "id", 10L);

        trainerProfile = TrainerProfile.create(
            trainer,
            "소개",
            5,
            Specialty.DIET,
            "서울 강남구",
            "https://example.com/profile.jpg"
        );
        ReflectionTestUtils.setField(trainerProfile, "id", 100L);
    }

    // 팔로우하지 않는 상태에서 팔로우 토글 시, 새로운 팔로우가 생성되는지 검증
    @Test
    void toggleFollow_createsFollowWhenNotExists() {
        when(userRepository.findByEmail(MEMBER_EMAIL)).thenReturn(Optional.of(member));
        when(trainerProfileRepository.findByUserId(trainer.getId())).thenReturn(Optional.of(trainerProfile));
        when(trainerFollowRepository.findByUserIdAndTrainerProfileId(member.getId(), trainerProfile.getId()))
            .thenReturn(Optional.empty());
        when(trainerFollowRepository.save(any(TrainerFollow.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        TrainerFollowToggleResponse response = trainerFollowService.toggleFollow(trainer.getId(), MEMBER_EMAIL);

        assertThat(response.followed()).isTrue();
        assertThat(response.message()).isEqualTo("팔로우 되었습니다.");

        ArgumentCaptor<TrainerFollow> followCaptor = ArgumentCaptor.forClass(TrainerFollow.class);
        verify(trainerFollowRepository).save(followCaptor.capture());
        TrainerFollow saved = followCaptor.getValue();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getTrainerProfile()).isEqualTo(trainerProfile);
    }

    // 이미 팔로우 중인 상태에서 팔로우 토글 시, 기존 팔로우가 삭제되는지 검증
    @Test
    void toggleFollow_removesFollowWhenAlreadyFollowing() {
        TrainerFollow existingFollow = TrainerFollow.create(member, trainerProfile);

        when(userRepository.findByEmail(MEMBER_EMAIL)).thenReturn(Optional.of(member));
        when(trainerProfileRepository.findByUserId(trainer.getId())).thenReturn(Optional.of(trainerProfile));
        when(trainerFollowRepository.findByUserIdAndTrainerProfileId(member.getId(), trainerProfile.getId()))
            .thenReturn(Optional.of(existingFollow));

        TrainerFollowToggleResponse response = trainerFollowService.toggleFollow(trainer.getId(), MEMBER_EMAIL);

        assertThat(response.followed()).isFalse();
        assertThat(response.message()).isEqualTo("팔로우가 취소되었습니다.");

        verify(trainerFollowRepository).delete(existingFollow);
        verify(trainerFollowRepository, never()).save(any(TrainerFollow.class));
    }

    // 사용자가 팔로우한 트레이너 목록을 조회 시, 요약 정보 DTO로 올바르게 변환되어 반환되는지 검증
    @Test
    void getFollowedTrainers_returnsMappedSummaries() {
        TrainerFollow follow = TrainerFollow.create(member, trainerProfile);

        when(userRepository.findByEmail(MEMBER_EMAIL)).thenReturn(Optional.of(member));
        when(trainerFollowRepository.findAllByUserId(member.getId())).thenReturn(List.of(follow));

        List<FollowedTrainerSummaryResponse> result = trainerFollowService.getFollowedTrainers(MEMBER_EMAIL);

        assertThat(result).hasSize(1);
        FollowedTrainerSummaryResponse summary = result.get(0);
        assertThat(summary.trainerId()).isEqualTo(trainerProfile.getId());
        assertThat(summary.name()).isEqualTo(trainer.getName());
        assertThat(summary.gymAddress()).isEqualTo(trainerProfile.getGymAddress());
        assertThat(summary.profileImageUrl()).isEqualTo(trainerProfile.getProfileImageUrl());
    }
}
