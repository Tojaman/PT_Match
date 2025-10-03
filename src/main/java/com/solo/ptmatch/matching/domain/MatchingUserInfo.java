package com.solo.ptmatch.matching.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingUserInfo {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    private MatchingUserInfo(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
    public static MatchingUserInfo of(String name, String email, String phone) {
        return new MatchingUserInfo(name, email, phone);
    }
}
