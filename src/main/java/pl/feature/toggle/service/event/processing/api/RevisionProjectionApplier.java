package pl.feature.toggle.service.event.processing.api;

public interface RevisionProjectionApplier {

    <T> void apply(RevisionProjectionPlan<T> plan);

}
