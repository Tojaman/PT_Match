package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TrainerProfileService {

    public List<TrainerSummaryResponse> getTrainerSummaries(TrainerSearchRequest trainerSearchRequest) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TrainerDetailResponse getTrainerDetail(Long trainerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TrainerProfileUpsertResponse registerTrainerProfile(TrainerProfileUpsertRequest trainerProfileRegisterRequest) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TrainerProfileUpsertResponse updateTrainerProfile(TrainerProfileUpsertRequest trainerProfileRegisterRequest) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
