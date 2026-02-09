package pl.feature.toggle.service.event.processing.internal;

import pl.feature.toggle.service.event.processing.api.RevisionProjectionApplier;
import pl.feature.toggle.service.event.processing.api.RevisionProjectionPlan;

public final class DefaultRevisionProjectionApplier implements RevisionProjectionApplier {

    public static RevisionProjectionApplier create() {
        return new DefaultRevisionProjectionApplier();
    }

    public <T> void apply(RevisionProjectionPlan<T> plan) {
        var currentOpt = plan.findCurrent().get();
        if (currentOpt.isEmpty()) {
            plan.onMissing().run();
            return;
        }

        var current = currentOpt.get();
        var currentRev = plan.currentRevision().apply(current);

        if (plan.incoming().isOutdatedComparedTo(currentRev)) {
            return;
        }

        if (plan.incoming().indicatesGapAfter(currentRev)) {
            if (plan.markInconsistentIfNotMarked().getAsBoolean()) {
                plan.publishRebuild().run();
            }
            return;
        }

        if (plan.incoming().canBeAppliedAfter(currentRev)) {
            plan.update().accept(current);
        }
    }

}
