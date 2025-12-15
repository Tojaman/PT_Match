package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findAllByTrainerProfileId(Long trainerProfileId);
}
