package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.trainer.domain.TrainerLike;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerLikeRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.LikedTrainerSummaryResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerLikeToggleResponse;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TrainerLikeService {
    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final TrainerLikeRepository trainerLikeRepository;

    @Transactional
    public TrainerLikeToggleResponse followTrainer(Long trainerId, String userEmail) {
        User user = findUserByEmail(userEmail);
        TrainerProfile trainerProfile = findTrainerById(trainerId);

        TrainerLike trainerLike = findTrainerLike(user.getId(), trainerProfile.getId());
        if (trainerLike != null) {
            return new TrainerLikeToggleResponse(true, "이미 좋아요한 트레이너입니다.");
        }

        return createFollow(user, trainerProfile);
    }

    @Transactional
    public TrainerLikeToggleResponse unfollowTrainer(Long trainerId, String userEmail) {
        User user = findUserByEmail(userEmail);
        TrainerProfile trainerProfile = findTrainerById(trainerId);

        TrainerLike trainerLike = findTrainerLike(user.getId(), trainerProfile.getId());
        if (trainerLike == null) {
            return new TrainerLikeToggleResponse(false, "이미 좋아요가 취소된 트레이너입니다.");
        }

        return cancelFollow(trainerProfile, trainerLike);
    }

    @Transactional(readOnly = true)
    public Page<LikedTrainerSummaryResponse> getFollowedTrainers(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Page<TrainerLike> trainerLikes = trainerLikeRepository.findAllByUserId(user.getId(), pageable);

        return trainerLikes.map(like -> LikedTrainerSummaryResponse.from(like.getTrainerProfile()));
    }

    @Transactional(readOnly = true)
    public long getFollowedUserCount(String userEmail) {
        User user = findUserByEmail(userEmail);

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        return trainerProfile.getLikesCount();
    }

    private User findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));
    }

    private TrainerProfile findTrainerById(Long trainerId) {
        return trainerProfileRepository.findById(trainerId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));
    }

    private TrainerLike findTrainerLike(Long userId, Long trainerId) {
        return trainerLikeRepository.findByUserIdAndTrainerProfileId(userId, trainerId)
                .orElse(null);
    }

    private TrainerLikeToggleResponse createFollow(User user, TrainerProfile trainerProfile) {
        changeTrainerLikeCount(trainerProfile.getId(), 1);
        trainerLikeRepository.save(TrainerLike.create(user, trainerProfile));
        return new TrainerLikeToggleResponse(true, "좋아요 되었습니다.");
    }

    private TrainerLikeToggleResponse cancelFollow(TrainerProfile trainerProfile, TrainerLike trainerLike) {
        changeTrainerLikeCount(trainerProfile.getId(), -1);
        trainerLikeRepository.delete(trainerLike);
        return new TrainerLikeToggleResponse(false, "좋아요가 취소되었습니다.");
    }

    private void changeTrainerLikeCount(Long trainerProfileId, int delta) {
        trainerProfileRepository.updateLikeCountAtomically(trainerProfileId, delta);
    }
}
