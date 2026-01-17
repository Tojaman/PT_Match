package com.solo.ptmatch.location.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.solo.ptmatch.location.domain.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query("SELECT l FROM Location l WHERE l.name LIKE CONCAT(:keyword, '%') ORDER BY l.name")
    List<Location> searchByNamePrefix(@Param("keyword") String keyword);
}
