package com.solo.ptmatch.statistics.presentation.response;

public record RecentReviewItem(
                Long reviewId,
                String userName,
                int rating,
                String content,
                String date) {
    public static RecentReviewItem of(Long reviewId, String userName, int rating, String content, String date) {
        return new RecentReviewItem(reviewId, userName, rating, content, date);
    }
}
