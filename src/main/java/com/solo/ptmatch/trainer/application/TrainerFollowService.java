package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.trainer.domain.TrainerFollow;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerFollowRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.FollowedTrainerSummaryResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerFollowToggleResponse;
import java.util.List;

import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TrainerFollowService {
    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final TrainerFollowRepository trainerFollowRepository;

    @Transactional
    public TrainerFollowToggleResponse toggleFollow(Long trainerId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findById(trainerId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        TrainerFollow trainerFollow = trainerFollowRepository.findByUserIdAndTrainerProfileId(user.getId(), trainerProfile.getId())
                .orElse(null);

        if (trainerFollow == null) {
            trainerFollowRepository.save(TrainerFollow.create(user, trainerProfile));
            return new TrainerFollowToggleResponse(true, "팔로우 되었습니다.");
        } else {
            trainerFollowRepository.delete(trainerFollow);
            return new TrainerFollowToggleResponse(false, "팔로우가 취소되었습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<FollowedTrainerSummaryResponse> getFollowedTrainers(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        return trainerFollowRepository.findAllByUserId(user.getId()).stream()
                .map(follow -> FollowedTrainerSummaryResponse.from(follow.getTrainerProfile()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long getFollowedUserCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        return trainerFollowRepository.countByTrainerProfileId(trainerProfile.getId());
    }
}
