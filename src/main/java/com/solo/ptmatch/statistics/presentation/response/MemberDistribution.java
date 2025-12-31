package com.solo.ptmatch.statistics.presentation.response;

public record MemberDistribution(
                int regularMembers,
                int reRegistered,
                int newMembers) {
    public static MemberDistribution of(int regularMembers, int reRegistered, int newMembers) {
        return new MemberDistribution(regularMembers, reRegistered, newMembers);
    }

    public static MemberDistribution empty() {
        return new MemberDistribution(0, 0, 0);
    }
}
