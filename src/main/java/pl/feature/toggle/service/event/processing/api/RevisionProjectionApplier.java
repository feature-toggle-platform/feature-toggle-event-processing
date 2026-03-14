package pl.feature.toggle.service.event.processing.api;

import pl.feature.toggle.service.event.processing.internal.RevisionApplierResult;

public interface RevisionProjectionApplier {

    <T> RevisionApplierResult apply(RevisionProjectionPlan<T> plan);

}
