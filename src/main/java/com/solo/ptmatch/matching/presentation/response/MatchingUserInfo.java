package com.solo.ptmatch.matching.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신청자(회원) 정보")
public record MatchingUserInfo(
    String name,
    String email,
    String phone
) {
    public static MatchingUserInfo from(com.solo.ptmatch.matching.domain.MatchingUserInfo userInfo) {
        return new MatchingUserInfo(userInfo.getName(), userInfo.getEmail(), userInfo.getPhone());
    }
}
