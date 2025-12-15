package com.solo.ptmatch.trainer.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.trainer.domain.Program;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.ProgramRepository;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.request.TrainerProgramUpsertRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerProgramResponse;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final ProgramRepository programRepository;

    public List<TrainerProgramResponse> getMyPrograms(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        List<Program> programs = programRepository.findAllByTrainerProfileId(trainerProfile.getId());
        return programs.stream()
                .map(TrainerProgramResponse::from)
                .toList();
    }

    @Transactional
    public TrainerProgramResponse registerProgram(String email, TrainerProgramUpsertRequest trainerProgramRequest) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        Program program = trainerProgramRequest.toEntity(trainerProfile);
        programRepository.save(program);

        return TrainerProgramResponse.from(program);
    }

    @Transactional
    public TrainerProgramResponse updateProgram(String email, Long programId,
            TrainerProgramUpsertRequest trainerProgramRequest) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PROGRAM_NOT_FOUND));

        // 소유권 검증: 프로그램이 현재 트레이너의 것인지 확인
        if (!program.getTrainerProfile().getId().equals(trainerProfile.getId())) {
            throw GlobalException.of(ErrorCode.FORBIDDEN);
        }

        program.update(trainerProgramRequest.title(), trainerProgramRequest.content());
        return TrainerProgramResponse.from(program);
    }

    @Transactional
    public void deleteProgram(String email, Long programId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PROGRAM_NOT_FOUND));

        // 소유권 검증: 프로그램이 현재 트레이너의 것인지 확인
        if (!program.getTrainerProfile().getId().equals(trainerProfile.getId())) {
            throw GlobalException.of(ErrorCode.FORBIDDEN);
        }

        programRepository.delete(program);
    }
}
