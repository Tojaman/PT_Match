package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.review.infrastructure.ReviewRepository;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.CertificationRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerCertificationResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerReviewResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TrainerProfileService {

    private final TrainerProfileRepository trainerProfileRepository;
    private final ReviewRepository reviewRepository;
    private final CertificationRepository certificationRepository;

    @Transactional(readOnly = true)
    public List<TrainerSummaryResponse> getTrainerSummaries(TrainerSearchRequest request) {
        Sort sort = createSort(request.sort());
        Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        Specialty specialty = request.specialty() != null ? Specialty.fromDescription(request.specialty()) : null;

        Page<TrainerProfile> trainerPage = trainerProfileRepository.searchBySpecialtyAndGymAddress(
                specialty,
                request.region(),
                pageable
        );

        return trainerPage.getContent().stream()
                .map(TrainerSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainerDetailResponse getTrainerDetail(Long trainerId) {
        TrainerProfile profile = trainerProfileRepository.findByTrainerId(trainerId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        // 트레이너 리뷰 Top5 조회
        List<TrainerReviewResponse> reviews = reviewRepository.findTop5ByTrainerProfileOrderByCreatedAtDesc(profile).stream()
                .map(TrainerReviewResponse::from)
                .toList();

        // 트레이너 자격증 조회
        List<TrainerCertificationResponse> certifications = certificationRepository.findAllByTrainerProfileId(profile.getId()).stream()
                .map(TrainerCertificationResponse::from)
                .toList();

        return TrainerDetailResponse.from(profile, reviews, certifications);
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
