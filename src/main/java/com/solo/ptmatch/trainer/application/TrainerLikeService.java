package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.trainer.domain.TrainerLike;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerLikeRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.LikedTrainerSummaryResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerLikeToggleResponse;
import java.util.List;

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
    public TrainerLikeToggleResponse toggleFollow(Long trainerId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findById(trainerId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        TrainerLike trainerLike = trainerLikeRepository.findByUserIdAndTrainerProfileId(user.getId(), trainerProfile.getId())
                .orElse(null);

        if (trainerLike == null) {
            trainerLikeRepository.save(TrainerLike.create(user, trainerProfile));
            trainerProfile.increaseLikes();
            return new TrainerLikeToggleResponse(true, "좋아요 되었습니다.");
        } else {
            trainerLikeRepository.delete(trainerLike);
            trainerProfile.decreaseLikes();
            return new TrainerLikeToggleResponse(false, "좋아요가 취소되었습니다.");
        }
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
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        return trainerProfile.getLikesCount();
    }
}
