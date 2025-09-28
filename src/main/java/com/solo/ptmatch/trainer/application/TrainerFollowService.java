package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.trainer.presentation.response.FollowedTrainerSummaryResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerFollowToggleResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TrainerFollowService {

    public TrainerFollowToggleResponse toggleFollow(Long trainerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<FollowedTrainerSummaryResponse> getFollowedTrainers() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
