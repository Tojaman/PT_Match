package com.solo.ptmatch.location.infrastructure;

import com.solo.ptmatch.location.domain.LegalDistrict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LegalDistrictRepository extends JpaRepository<LegalDistrict, String> {

    // 법정동 이름으로 검색 (자동완성용)
    List<LegalDistrict> findByNameContainingOrderByName(String keyword);
}
