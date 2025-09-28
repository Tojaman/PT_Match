package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.matching.domain.Matching;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
}
