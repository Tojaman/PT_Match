package com.solo.ptmatch.matching.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public enum ReservationStatus {
    PENDING_APPROVAL(EnumSet.of(SCHEDULED, CANCELED)),
    SCHEDULED(EnumSet.of(COMPLETED, CANCELED)),
    COMPLETED(Collections.emptySet()),
    CANCELED(Collections.emptySet());

    private final Set<ReservationStatus> nextStatuses;

    ReservationStatus(Set<ReservationStatus> nextStatuses) {
        this.nextStatuses = nextStatuses;
    }

    public boolean canTransitionTo(ReservationStatus target) {
        Objects.requireNonNull(target, "target must not be null");
        return target == this || nextStatuses.contains(target);
    }

    public void ensureTransitionAllowed(ReservationStatus target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException("Cannot transition from " + this + " to " + target);
        }
    }
}
