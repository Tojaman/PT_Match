package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.review.infrastructure.ReviewRepository;
import com.solo.ptmatch.trainer.domain.Certification;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.CertificationRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerCertificationResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;

import java.util.List;

import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
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
    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public Page<TrainerSummaryResponse> getTrainerSummaries(TrainerSearchRequest request) {
        Sort sort = createSort(request.sort());
        Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        Specialty specialty = request.specialty() != null ? Specialty.fromDescription(request.specialty()) : null;

        Page<TrainerProfile> trainerPage = trainerProfileRepository.searchBySpecialtyAndGymAddress(
                specialty,
                request.region(),
                pageable
        );

        // Page 인터페이스에 map() 메서드 정의되어 있음(페이지 정보는 그대로 복사하고, 내용물(`List`)에만 변환 함수를 적용하여, 새로운 `Page` 객체를 반환)
        return trainerPage.map(TrainerSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public TrainerDetailResponse getTrainerDetail(Long trainerId) {
        TrainerProfile profile = trainerProfileRepository.findByTrainerId(trainerId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        // 트레이너 자격증 조회
        List<TrainerCertificationResponse> certifications = certificationRepository.findAllByTrainerProfileId(profile.getId()).stream()
                .map(TrainerCertificationResponse::from)
                .toList();

        return TrainerDetailResponse.from(profile, certifications);
    }

    @Transactional
    public TrainerProfileUpsertResponse registerTrainerProfile(
            String email,
            TrainerProfileUpsertRequest request
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        trainerProfileRepository.findByTrainerId(user.getId())
                .ifPresent(profile -> {
                    throw GlobalException.of(ErrorCode.CONFLICT);
                });

        TrainerProfile savedProfile = trainerProfileRepository.save(request.toEntity(user));

        if (request.certifications() != null && !request.certifications().isEmpty()) {
            List<Certification> certifications = request.certifications().stream()
                    .map(cert -> cert.toEntity(savedProfile)) // TrainerCertificationRequest에 toEntity가 있다고 가정
                    .toList();
            certificationRepository.saveAll(certifications);
        }

        return TrainerProfileUpsertResponse.from(savedProfile);
    }

    @Transactional
    public TrainerProfileUpsertResponse updateTrainerProfile(
            String email,
            TrainerProfileUpsertRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByTrainerId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        profile.updateProfile(
                request.bio(),
                request.careerYears(),
                request.specialties(),
                request.gymAddress(),
                request.profileImageUrl()
        );

        // 프로필 수정 시 기존 자격증 삭제 후 재등록
        certificationRepository.deleteByTrainerProfileId(profile.getId());
        if (request.certifications() != null && !request.certifications().isEmpty()) {
            List<Certification> certifications = request.certifications().stream()
                    .map(cert -> cert.toEntity(profile))
                    .toList();
            certificationRepository.saveAll(certifications);
        }

        return TrainerProfileUpsertResponse.from(profile);
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
