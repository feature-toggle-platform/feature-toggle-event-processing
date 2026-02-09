package pl.feature.toggle.service.event.processing.api;

import lombok.Getter;
import lombok.experimental.Accessors;
import pl.feature.toggle.service.model.Revision;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Accessors(fluent = true)
public final class RevisionProjectionPlan<T> {

    final Revision incoming;
    final Supplier<Optional<T>> findCurrent;

    final Runnable onMissing;

    final Function<T, Revision> currentRevision;
    final Consumer<T> update;
    final BooleanSupplier markInconsistentIfNotMarked;
    final Runnable publishRebuild;

    private RevisionProjectionPlan(Builder<T> builder) {
        this.incoming = builder.incoming;
        this.findCurrent = builder.findCurrent;
        this.onMissing = builder.onMissing;
        this.currentRevision = builder.currentRevision;
        this.update = builder.update;
        this.markInconsistentIfNotMarked = builder.markInconsistentIfNotMarked;
        this.publishRebuild = builder.publishRebuild;
    }

    public static <T> Builder<T> forIncoming(Revision incoming) {
        return new Builder<>(incoming);
    }

    public static final class Builder<T> {

        private final Revision incoming;

        private Supplier<Optional<T>> findCurrent;

        private Runnable onMissing;

        private Function<T, Revision> currentRevision;
        private Consumer<T> update;
        private BooleanSupplier markInconsistentIfNotMarked;
        private Runnable publishRebuild;

        private Builder(Revision incoming) {
            this.incoming = incoming;
        }

        public Builder<T> findCurrentUsing(Supplier<Optional<T>> finder) {
            this.findCurrent = finder;
            return this;
        }

        public Builder<T> onMissing(Runnable action) {
            this.onMissing = action;
            return this;
        }

        public Builder<T> extractCurrentRevisionUsing(Function<T, Revision> extractor) {
            this.currentRevision = extractor;
            return this;
        }

        public Builder<T> applyUpdateWhenApplicable(Consumer<T> updateAction) {
            this.update = updateAction;
            return this;
        }

        public Builder<T> markInconsistentWhenGapDetectedIfNotMarked(BooleanSupplier action) {
            this.markInconsistentIfNotMarked = action;
            return this;
        }

        public Builder<T> publishRebuildWhenGapDetected(Runnable action) {
            this.publishRebuild = action;
            return this;
        }

        public RevisionProjectionPlan<T> build() {
            require(findCurrent, "findCurrentUsing");
            require(currentRevision, "extractCurrentRevisionUsing");
            require(update, "applyUpdateWhenApplicable");
            require(markInconsistentIfNotMarked, "markInconsistentWhenGapDetectedIfNotMarked");
            require(publishRebuild, "publishRebuildWhenGapDetected");
            require(onMissing, "onMissing");

            return new RevisionProjectionPlan<>(this);
        }

        private static void require(Object value, String name) {
            if (value == null) {
                throw new IllegalStateException("Missing required configuration: " + name);
            }
        }
    }
}