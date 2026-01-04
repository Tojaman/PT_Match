package com.solo.ptmatch.matching.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "matching")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Matching extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_status", nullable = false)
    private MatchingStatus matchingStatus;

    @Embedded
    private MatchingUserInfo matchingUserInfo;

    @OneToMany(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<MatchingSchedule> schedules = new ArrayList<>();

    @Column(name = "remaining_sessions")
    private int remainingSessions; // 잔여 횟수

    public void addSchedule(MatchingSchedule schedule) {
        schedule.assignMatching(this);
        schedules.add(schedule);
    }

    @Column(name = "price_per_session")
    private Integer pricePerSession;

    private Matching(User user, TrainerProfile trainerProfile, String message, MatchingUserInfo matchingUserInfo,
            Integer pricePerSession) {
        this.user = user;
        this.trainerProfile = trainerProfile;
        this.message = message;
        this.matchingStatus = MatchingStatus.PENDING;
        this.matchingUserInfo = matchingUserInfo;
        this.pricePerSession = pricePerSession;
    }

    public static Matching create(User user, TrainerProfile trainerProfile, String message,
            MatchingUserInfo matchingUserInfo, Integer pricePerSession) {
        return new Matching(user, trainerProfile, message, matchingUserInfo, pricePerSession);
    }

    public void accept() {

        this.matchingStatus = MatchingStatus.ACCEPTED;
    }

    public void reject() {
        this.matchingStatus = MatchingStatus.REJECTED;
    }

    public void complete() {
        this.matchingStatus = MatchingStatus.COMPLETED;
    }

    public void completeSession() {
        if (remainingSessions > 0) {
            remainingSessions--;
        }
    }

    public boolean canCreateReservation() {
        return matchingStatus == MatchingStatus.ACCEPTED;
    }
}
