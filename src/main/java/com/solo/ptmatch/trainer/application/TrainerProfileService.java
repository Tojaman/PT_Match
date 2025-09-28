package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.trainer.application.dto.FindTrainersQuery;
import com.solo.ptmatch.trainer.application.dto.TrainerDetailResult;
import com.solo.ptmatch.trainer.application.dto.TrainerProfileUpsertCommand;
import com.solo.ptmatch.trainer.application.dto.TrainerProfileUpsertResult;
import com.solo.ptmatch.trainer.application.dto.TrainerSummaryResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TrainerProfileService {

    public List<TrainerSummaryResult> getTrainerSummaries(FindTrainersQuery query) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TrainerDetailResult getTrainerDetail(Long trainerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TrainerProfileUpsertResult registerTrainerProfile(TrainerProfileUpsertCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TrainerProfileUpsertResult updateTrainerProfile(TrainerProfileUpsertCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
