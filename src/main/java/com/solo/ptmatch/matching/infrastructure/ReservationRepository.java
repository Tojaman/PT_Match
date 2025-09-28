package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.matching.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
