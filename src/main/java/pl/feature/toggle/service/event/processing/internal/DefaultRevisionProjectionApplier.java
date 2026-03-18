package pl.feature.toggle.service.event.processing.internal;

import lombok.extern.slf4j.Slf4j;
import pl.feature.toggle.service.event.processing.api.RevisionProjectionApplier;
import pl.feature.toggle.service.event.processing.api.RevisionProjectionPlan;
import pl.feature.toggle.service.event.processing.internal.exception.RevisionProjectionApplierException;

@Slf4j
public final class DefaultRevisionProjectionApplier implements RevisionProjectionApplier {

    public static RevisionProjectionApplier create() {
        return new DefaultRevisionProjectionApplier();
    }

    public <T> RevisionApplierResult apply(RevisionProjectionPlan<T> plan) {
        var currentOpt = plan.findCurrent().get();

        if (currentOpt.isEmpty()) {
            log.info(
                    "Projection create applied for missing entity: eventId={}, incomingRevision={}",
                    plan.eventId(),
                    plan.incoming()
            );
            plan.onMissing().run();
            return RevisionApplierResult.applied();
        }

        var current = currentOpt.get();
        var currentRev = plan.currentRevision().apply(current);

        if (plan.incoming().isOutdatedComparedTo(currentRev)) {
            log.warn(
                    "Stale projection event ignored: eventId={}, incomingRevision={}, currentRevision={}",
                    plan.eventId(),
                    plan.incoming(),
                    currentRev
            );
            return RevisionApplierResult.ignoredStaleEvent();
        }

        if (plan.incoming().indicatesGapAfter(currentRev)) {
            log.warn(
                    "Projection gap detected: eventId={}, incomingRevision={}, currentRevision={}",
                    plan.eventId(),
                    plan.incoming(),
                    currentRev
            );

            if (plan.markInconsistentIfNotMarked().getAsBoolean()) {
                log.warn(
                        "Projection marked inconsistent and rebuild requested: eventId={}, incomingRevision={}, currentRevision={}",
                        plan.eventId(),
                        plan.incoming(),
                        currentRev
                );
                plan.publishRebuild().run();
            } else {
                log.warn(
                        "Projection already marked inconsistent, rebuild not requested again: eventId={}, incomingRevision={}, currentRevision={}",
                        plan.eventId(),
                        plan.incoming(),
                        currentRev
                );
            }

            return RevisionApplierResult.gapDetected();
        }

        if (plan.incoming().canBeAppliedAfter(currentRev)) {
            log.info(
                    "Projection update applied: eventId={}, incomingRevision={}, previousRevision={}",
                    plan.eventId(),
                    plan.incoming(),
                    currentRev
            );
            plan.update().accept(current);
            return RevisionApplierResult.applied();
        }

        log.error(
                "Unhandled revision transition: eventId={}, incomingRevision={}, currentRevision={}",
                plan.eventId(),
                plan.incoming(),
                currentRev
        );

        throw new RevisionProjectionApplierException(plan.incoming(), currentRev);
    }
}
