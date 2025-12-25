package com.solo.ptmatch.trainer.presentation.response;

public record MemberStatsDto(int newMembersThisMonth, int newMembersLastMonth, int totalMembers) {
    public static MemberStatsDto of(int newMembersThisMonth, int newMembersLastMonth, int totalMembers) {
        return new MemberStatsDto(newMembersThisMonth, newMembersLastMonth, totalMembers);
    }

    public String getGrowthRate() {
        if (newMembersLastMonth == 0) {
            return newMembersThisMonth > 0 ? "+100%" : "0%";
        }
        int rate = ((newMembersThisMonth - newMembersLastMonth) * 100) / newMembersLastMonth;
        return (rate >= 0 ? "+" : "") + rate + "%";
    }
}
