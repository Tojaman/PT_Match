package com.solo.ptmatch.location.application;

import com.solo.ptmatch.location.domain.Location;
import com.solo.ptmatch.location.infrastructure.LocationRepository;
import com.solo.ptmatch.location.presentation.dto.LocationSearchResponse;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class LocationSearchService {

    private final LocationRepository locationRepository;
    private final TrainerProfileRepository trainerProfileRepository;

    @Transactional(readOnly = true)
    public List<LocationSearchResponse> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        List<LocationSearchResponse> results = new ArrayList<>();

        // 1. locations 테이블에서 검색 (지하철역, 동 주소)
        List<Location> locations = locationRepository.searchByNamePrefix(keyword.trim());
        for (Location location : locations) {
            results.add(LocationSearchResponse.from(location));
        }

        // 2. trainer_profiles 테이블에서 gymName 검색
        List<TrainerProfile> gyms = trainerProfileRepository.findByGymNameStartingWithOrderByGymName(keyword.trim());

        Set<String> seenGymNames = new HashSet<>();
        for (TrainerProfile gym : gyms) {
            if (seenGymNames.add(gym.getGymName())) {
                results.add(LocationSearchResponse.from(gym));
            }
        }

        return results;
    }
}
