package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.util.S2Util;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 기존 TrainerProfile 데이터의 S2 Cell ID 일괄 업데이트 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class S2CellIdMigrationService {

    private final TrainerProfileRepository trainerProfileRepository;

    /**
     * 모든 TrainerProfile의 s2_cell_id를 일괄 계산하여 업데이트
     * 
     * @return 업데이트된 프로필 수
     */
    @Transactional
    public int migrateAllProfiles() {
        List<TrainerProfile> profiles = trainerProfileRepository.findAll();

        int updatedCount = 0;
        for (TrainerProfile profile : profiles) {
            if (profile.getS2CellId() == null) {
                long s2CellId = S2Util.calculateS2CellId(
                        profile.getLatitude(),
                        profile.getLongitude());
                profile.updateS2CellId(s2CellId);
                updatedCount++;
            }
        }

        log.info("S2 Cell ID 마이그레이션 완료: {}개 프로필 업데이트", updatedCount);
        return updatedCount;
    }

    /**
     * 모든 TrainerProfile의 s2_cell_id를 강제로 재계산
     * 
     * @return 업데이트된 프로필 수
     */
    @Transactional
    public int recalculateAllProfiles() {
        List<TrainerProfile> profiles = trainerProfileRepository.findAll();

        for (TrainerProfile profile : profiles) {
            long s2CellId = S2Util.calculateS2CellId(
                    profile.getLatitude(),
                    profile.getLongitude());
            profile.updateS2CellId(s2CellId);
        }

        log.info("S2 Cell ID 재계산 완료: {}개 프로필 업데이트", profiles.size());
        return profiles.size();
    }
}
