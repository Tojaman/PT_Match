package com.solo.ptmatch.trainer.application.dto;

public record TrainerReviewSnippetResult(
    Long reviewId,
    String reviewerName,
    int rating,
    String content
) {
}
