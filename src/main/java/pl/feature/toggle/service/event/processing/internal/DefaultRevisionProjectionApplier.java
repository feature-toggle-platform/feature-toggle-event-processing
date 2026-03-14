package pl.feature.toggle.service.event.processing.internal;

import pl.feature.toggle.service.event.processing.api.RevisionProjectionApplier;
import pl.feature.toggle.service.event.processing.api.RevisionProjectionPlan;

public final class DefaultRevisionProjectionApplier implements RevisionProjectionApplier {

    public static RevisionProjectionApplier create() {
        return new DefaultRevisionProjectionApplier();
    }

    public <T> RevisionApplierResult apply(RevisionProjectionPlan<T> plan) {
        var currentOpt = plan.findCurrent().get();

        if (currentOpt.isEmpty()) {
            plan.onMissing().run();
            return RevisionApplierResult.applied();
        }

        var current = currentOpt.get();
        var currentRev = plan.currentRevision().apply(current);

        if (plan.incoming().isOutdatedComparedTo(currentRev)) {
            return RevisionApplierResult.ignoredStaleEvent();
        }

        if (plan.incoming().indicatesGapAfter(currentRev)) {
            if (plan.markInconsistentIfNotMarked().getAsBoolean()) {
                plan.publishRebuild().run();
            }
            return RevisionApplierResult.gapDetected();
        }

        if (plan.incoming().canBeAppliedAfter(currentRev)) {
            plan.update().accept(current);
            return RevisionApplierResult.applied();
        }

        throw new IllegalStateException(
                "Unhandled revision transition. Incoming revision: " + plan.incoming() + ", current revision: " + currentRev
        );
    }

}
