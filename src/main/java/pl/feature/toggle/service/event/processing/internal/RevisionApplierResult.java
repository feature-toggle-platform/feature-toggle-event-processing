package pl.feature.toggle.service.event.processing.internal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RevisionApplierResult {

    private final Status status;

    static RevisionApplierResult applied() {
        return new RevisionApplierResult(Status.APPLIED);
    }

    static RevisionApplierResult gapDetected() {
        return new RevisionApplierResult(Status.GAP_DETECTED);
    }

    static RevisionApplierResult ignoredStaleEvent() {
        return new RevisionApplierResult(Status.IGNORED_STALE_EVENT);
    }

    public boolean wasApplied() {
        return status == Status.APPLIED;
    }

    public boolean wasGapDetected() {
        return status == Status.GAP_DETECTED;
    }

    public boolean wasIgnored() {
        return status == Status.IGNORED_STALE_EVENT;
    }

    enum Status {
        APPLIED,
        IGNORED_STALE_EVENT,
        GAP_DETECTED
    }
}
