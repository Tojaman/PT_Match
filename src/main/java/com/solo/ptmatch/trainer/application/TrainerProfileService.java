package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.common.storage.ObjectStorageService;
import com.solo.ptmatch.common.storage.PresignedUpload;
import com.solo.ptmatch.common.storage.PresignedUploadCommand;
import com.solo.ptmatch.product.presentation.request.PresignedUrlRequest;
import com.solo.ptmatch.product.presentation.response.PresignedUrlResponse;
import com.solo.ptmatch.trainer.domain.Certification;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.domain.TrainerImage;
import com.solo.ptmatch.trainer.domain.GymImage;
import com.solo.ptmatch.trainer.infrastructure.CertificationRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerImageRepository;
import com.solo.ptmatch.trainer.infrastructure.GymImageRepository;
import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import com.solo.ptmatch.trainer.presentation.request.GymImageRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerImageRequest;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import com.solo.ptmatch.common.cache.enums.CacheTarget;
import com.solo.ptmatch.common.cache.facade.CacheFacade;
import com.solo.ptmatch.common.cache.pubsub.RedisMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerProfileService {

    private final TrainerProfileRepository trainerProfileRepository;
    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;
    private final TrainerImageRepository trainerImageRepository;
    private final GymImageRepository gymImageRepository;
    private final ObjectStorageService objectStorageService;
    private final CacheFacade cacheFacade;
    private final RedisMessagePublisher messagePublisher;

    @Transactional(readOnly = true)
    public Page<TrainerSummaryResponse> getTrainerSummaries(TrainerSearchRequest request, Pageable pageable) {

        return switch (request.locationType()) {
            case FACILITY -> {
                yield trainerProfileRepository.findByFacilityName(request.facilityName(), pageable)
                        .map(TrainerSummaryResponse::from);
            }
            case SUBWAY -> {
                Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(),
                        pageable.getPageSize());
                yield trainerProfileRepository
                        .findNearby(request.longitude(), request.latitude(), 3000,
                                unsortedPageable)
                        .map(TrainerSummaryResponse::from);
            }
            case DISTRICT -> {
                yield trainerProfileRepository.findByDistrictCode(request.districtCode(), pageable)
                        .map(TrainerSummaryResponse::from);
            }
        };
    }

    @Transactional(readOnly = true)
    public TrainerDetailResponse getTrainerDetail(Long trainerId) {
        TrainerProfile profile = trainerProfileRepository.findById(trainerId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        List<TrainerImage> trainerImages = trainerImageRepository.findAllByTrainerProfileId(profile.getId());
        List<GymImage> gymImages = gymImageRepository.findAllByTrainerProfileId(profile.getId());
        List<Certification> certifications = certificationRepository.findAllByTrainerProfileId(profile.getId());

        return TrainerDetailResponse.from(profile, trainerImages, gymImages, certifications);
    }

    @Transactional(readOnly = true)
    public Long getTrainerId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        return profile.getId();
    }

    @Transactional(readOnly = true)
    public List<TrainerSummaryResponse> getPopularTrainers(double latitude, double longitude, double radiusMeters,
                                                           int limit) {
        List<TrainerProfile> trainers = trainerProfileRepository.findPopularTrainersNearby(latitude, longitude,
                radiusMeters, limit);
        return trainers.stream()
                .map(TrainerSummaryResponse::from)
                .toList();
    }

    @Transactional
    public TrainerProfileUpsertResponse registerTrainerProfile(String email, TrainerProfileUpsertRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        trainerProfileRepository.findByUserId(user.getId())
                .ifPresent(profile -> {
                    throw GlobalException.of(ErrorCode.CONFLICT);
                });

        TrainerProfile savedProfile = trainerProfileRepository.save(request.toEntity(user));

        List<TrainerImage> trainerImages = request.trainerImages().stream()
                .map(image -> image.toEntity(savedProfile))
                .toList();
        trainerImageRepository.saveAll(trainerImages);

        List<GymImage> gymImages = request.gymImages().stream()
                .map(image -> image.toEntity(savedProfile))
                .toList();
        gymImageRepository.saveAll(gymImages);

        List<Certification> certifications = request.certifications().stream()
                .map(cert -> cert.toEntity(savedProfile))
                .toList();
        certificationRepository.saveAll(certifications);

        evictCacheAfterCommit(savedProfile.getSportType(), savedProfile.getS2CellId());

        return TrainerProfileUpsertResponse.from(savedProfile, trainerImages, gymImages, certifications);
    }

    @Transactional
    public TrainerProfileUpsertResponse updateTrainerProfile(String email, TrainerProfileUpsertRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        SportType oldSportType = profile.getSportType();
        Long oldCellId = profile.getS2CellId();

        profile.updateProfile(
                request.bio(),
                request.careerYears(),
                request.sportType(),
                request.facilityName(),
                request.facilityAddress(),
                request.latitude(),
                request.longitude(),
                request.trainerImages().get(0).imageUrl(), // 첫 번째 이미지 썸네일로 설정
                request.pricePerSession());

        SportType newSportType = profile.getSportType();
        Long newCellId = profile.getS2CellId();

        // 기존 캐시 삭제 (Pub/Sub 적용)
        if (!oldCellId.equals(newCellId) || !oldSportType.equals(newSportType)) {
            evictCacheAfterCommit(newSportType, newCellId);
            evictCacheAfterCommit(oldSportType, oldCellId);
        }

        // 프로필 수정 시 기존 자격증 삭제 후 재등록
        certificationRepository.deleteByTrainerProfileId(profile.getId());
        List<Certification> certifications = new ArrayList<>();
        if (request.certifications() != null && !request.certifications().isEmpty()) {
            List<Certification> newCertifications = request.certifications().stream()
                    .map(cert -> cert.toEntity(profile))
                    .toList();
            certifications = certificationRepository.saveAll(newCertifications);
        }

        // 이미지 부분 업데이트
        List<TrainerImage> trainerImages = updateTrainerImages(profile, request.trainerImages());
        List<GymImage> gymImages = updateGymImages(profile, request.gymImages());

        return TrainerProfileUpsertResponse.from(profile, trainerImages, gymImages, certifications);
    }

    @Transactional(readOnly = true)
    public PresignedUrlResponse issuePresignedUrl(PresignedUrlRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        PresignedUploadCommand command = new PresignedUploadCommand(user.getId(), request.fileName(),
                request.contentType(), "productImages");
        PresignedUpload upload = objectStorageService.issuePresignedUpload(command);
        return PresignedUrlResponse.from(upload);
    }

    private List<TrainerImage> updateTrainerImages(TrainerProfile profile,
                                                   List<TrainerImageRequest> imageRequests) {
        if (imageRequests == null) {
            imageRequests = new ArrayList<>();
        }

        List<TrainerImage> currentImages = trainerImageRepository.findAllByTrainerProfileId(profile.getId());
        Map<String, TrainerImageRequest> requestMap = imageRequests.stream()
                .collect(Collectors.toMap(TrainerImageRequest::imageUrl, Function.identity()));

        List<TrainerImage> imagesToDelete = new ArrayList<>();
        List<TrainerImage> keptImages = new ArrayList<>();

        for (TrainerImage image : currentImages) {
            if (requestMap.containsKey(image.getImageUrl())) {
                // 기존 이미지 존재 -> 순서 업데이트 및 유지
                TrainerImageRequest req = requestMap.get(image.getImageUrl());
                image.updateDisplayOrder(req.displayOrder());
                requestMap.remove(image.getImageUrl());
                keptImages.add(image);
            } else {
                // 요청에 없음 -> 삭제 대상
                imagesToDelete.add(image);
            }
        }

        // 삭제
        trainerImageRepository.deleteAll(imagesToDelete);

        // 신규 추가 (Map에 남은 항목들)
        List<TrainerImage> imagesToAdd = requestMap.values().stream()
                .map(req -> req.toEntity(profile))
                .toList();
        List<TrainerImage> savedNewImages = trainerImageRepository.saveAll(imagesToAdd);

        // 최종 리스트 합치기 (유지된 것 + 새로 추가된 것)
        keptImages.addAll(savedNewImages);
        keptImages.sort(Comparator.comparingInt(TrainerImage::getDisplayOrder));

        return keptImages;
    }

    private List<GymImage> updateGymImages(TrainerProfile profile, List<GymImageRequest> imageRequests) {
        if (imageRequests == null) {
            imageRequests = new ArrayList<>();
        }

        List<GymImage> currentImages = gymImageRepository.findAllByTrainerProfileId(profile.getId());
        Map<String, GymImageRequest> requestMap = imageRequests.stream()
                .collect(Collectors.toMap(GymImageRequest::imageUrl, Function.identity()));

        List<GymImage> imagesToDelete = new ArrayList<>();
        List<GymImage> keptImages = new ArrayList<>();

        for (GymImage image : currentImages) {
            if (requestMap.containsKey(image.getImageUrl())) {
                // 기존 이미지 존재 -> 순서 업데이트 및 유지
                GymImageRequest req = requestMap.get(image.getImageUrl());
                image.updateDisplayOrder(req.displayOrder());
                requestMap.remove(image.getImageUrl());
                keptImages.add(image);
            } else {
                // 요청에 없음 -> 삭제 대상
                imagesToDelete.add(image);
            }
        }

        // 삭제
        gymImageRepository.deleteAll(imagesToDelete);

        // 신규 추가
        List<GymImage> imagesToAdd = requestMap.values().stream()
                .map(req -> req.toEntity(profile))
                .toList();
        List<GymImage> savedNewImages = gymImageRepository.saveAll(imagesToAdd);

        // 최종 리스트 합치기
        keptImages.addAll(savedNewImages);
        keptImages.sort(Comparator.comparingInt(GymImage::getDisplayOrder));

        return keptImages;
    }

    private void evictCache(String sportType, String cellId) {
        String key = sportType + ":" + cellId;
        cacheFacade.evict(CacheTarget.TRAINER_COUNT, key);
        cacheFacade.evict(CacheTarget.TRAINER_MARKER, key);

        // Pub/Sub으로 변경 전파
        messagePublisher.publish(CacheTarget.TRAINER_COUNT, key);
        messagePublisher.publish(CacheTarget.TRAINER_MARKER, key);
    }

    private void evictCacheAfterCommit(SportType sportType, Long cellId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictCache(sportType.name(), String.valueOf(cellId));
                }
            });
        } else {
            evictCache(sportType.name(), String.valueOf(cellId));
        }
    }
}
