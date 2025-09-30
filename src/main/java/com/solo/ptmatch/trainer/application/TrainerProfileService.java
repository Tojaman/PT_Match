package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TrainerProfileService {

    private final TrainerProfileRepository trainerProfileRepository;

    public List<TrainerSummaryResponse> getTrainerSummaries(TrainerSearchRequest request) {
        Sort sort = createSort(request.sort());
        Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        Specialty specialty = request.specialty() != null ? Specialty.valueOf(request.specialty().toUpperCase()) : null;

        Page<TrainerProfile> trainerPage = trainerProfileRepository.searchBySpecialtyAndGymAddress(
                specialty,
                request.region(),
                pageable
        );

        return trainerPage.getContent().stream()
                .map(TrainerSummaryResponse::from)
                .toList();
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

    private Sort createSort(String sortString) {
        if (sortString == null || sortString.isBlank()) {
            return Sort.unsorted();
        }

        String[] parts = sortString.split("_");
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.fromString(parts[1]); // 정렬(오름/내림차순)
        return Sort.by(direction, property);
    }
}
