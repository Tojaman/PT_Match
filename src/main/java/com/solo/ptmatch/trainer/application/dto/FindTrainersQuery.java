package com.solo.ptmatch.trainer.application.dto;

public record FindTrainersQuery(
    String specialty,
    String region,
    String sort,
    Integer page,
    Integer size
) {
}
