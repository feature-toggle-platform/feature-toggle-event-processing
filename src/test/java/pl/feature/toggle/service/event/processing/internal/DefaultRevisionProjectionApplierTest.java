package pl.feature.toggle.service.event.processing.internal;

import org.junit.jupiter.api.Test;
import pl.feature.toggle.service.event.processing.api.RevisionProjectionApplier;
import pl.feature.toggle.service.event.processing.api.RevisionProjectionPlan;
import pl.feature.toggle.service.model.Revision;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRevisionProjectionApplierTest {

    private final RevisionProjectionApplier sut = new DefaultRevisionProjectionApplier();

    @Test
    void should_insert_when_missing_and_incoming_is_initial() {
        // given
        var fx = Fixture.missing();
        var incoming = Revision.initialRevision();

        // when
        fx.apply(incoming);

        // then
        fx.assertInsertedOnce();
        fx.assertNoUpdates();
        fx.assertNoMarkInconsistentCalls();
        fx.assertNoPublish();
    }

    @Test
    void should_update_when_incoming_is_next_revision() {
        // given
        var current = new RefStub(Revision.from(2));
        var fx = Fixture.existing(current);
        var incoming = current.lastRevision().next();

        // when
        fx.apply(incoming);

        // then
        fx.assertUpdatedOnce();
        fx.assertNoInsert();
        fx.assertNoMarkInconsistentCalls();
        fx.assertNoPublish();
    }

    @Test
    void should_do_nothing_when_event_is_duplicate() {
        // given
        var current = new RefStub(Revision.from(2));
        var fx = Fixture.existing(current);
        var incoming = Revision.from(2);

        // when
        fx.apply(incoming);

        // then
        fx.assertNoInsert();
        fx.assertNoUpdates();
        fx.assertNoMarkInconsistentCalls();
        fx.assertNoPublish();
    }

    @Test
    void should_do_nothing_when_event_is_outdated() {
        // given
        var current = new RefStub(Revision.from(5));
        var fx = Fixture.existing(current);
        var incoming = Revision.from(2);

        // when
        fx.apply(incoming);

        // then
        fx.assertNoInsert();
        fx.assertNoUpdates();
        fx.assertNoMarkInconsistentCalls();
        fx.assertNoPublish();
    }

    @Test
    void should_mark_inconsistent_and_publish_when_gap_detected_and_first_time_marked() {
        // given
        var current = new RefStub(Revision.from(2));
        var fx = Fixture.existing(current).markReturns(true);
        var incoming = Revision.from(5); // gap > next

        // when
        fx.apply(incoming);

        // then
        fx.assertNoInsert();
        fx.assertNoUpdates();
        fx.assertMarkInconsistentCalledOnce();
        fx.assertPublishedOnce();
    }

    @Test
    void should_not_publish_when_gap_detected_but_already_marked_inconsistent() {
        // given
        var current = new RefStub(Revision.from(2));
        var fx = Fixture.existing(current).markReturns(false);
        var incoming = Revision.from(5); // gap > next

        // when
        fx.apply(incoming);

        // then
        fx.assertNoInsert();
        fx.assertNoUpdates();
        fx.assertMarkInconsistentCalledOnce();
        fx.assertNoPublish();
    }

// ===== fixture =====

    private static final class RefStub {
        private final Revision lastRevision;

        private RefStub(Revision lastRevision) {
            this.lastRevision = lastRevision;
        }

        Revision lastRevision() {
            return lastRevision;
        }
    }

    private static final class Fixture {

        private final RevisionProjectionApplier sut = new DefaultRevisionProjectionApplier();

        private RefStub current;

        private int insertCalls;
        private int updateCalls;
        private int markCalls;
        private int publishCalls;

        private boolean markReturn = true;

        static Fixture missing() {
            var fx = new Fixture();
            fx.current = null;
            return fx;
        }

        static Fixture existing(RefStub current) {
            var fx = new Fixture();
            fx.current = current;
            return fx;
        }

        Fixture markReturns(boolean value) {
            this.markReturn = value;
            return this;
        }

        void apply(Revision incoming) {
            var plan = RevisionProjectionPlan.<RefStub>forIncoming(incoming)
                    .findCurrentUsing(() -> Optional.ofNullable(current))
                    .insertWhenMissing(() -> insertCalls++)
                    .extractCurrentRevisionUsing(RefStub::lastRevision)
                    .applyUpdateWhenApplicable(__ -> updateCalls++)
                    .markInconsistentWhenGapDetectedIfNotMarked(() -> {
                        markCalls++;
                        return markReturn;
                    })
                    .publishRebuildWhenGapDetected(() -> publishCalls++)
                    .build();

            sut.apply(plan);
        }

        void assertInsertedOnce() {
            assertThat(insertCalls).isEqualTo(1);
        }

        void assertNoInsert() {
            assertThat(insertCalls).isEqualTo(0);
        }

        void assertUpdatedOnce() {
            assertThat(updateCalls).isEqualTo(1);
        }

        void assertNoUpdates() {
            assertThat(updateCalls).isEqualTo(0);
        }

        void assertMarkInconsistentCalledOnce() {
            assertThat(markCalls).isEqualTo(1);
        }

        void assertNoMarkInconsistentCalls() {
            assertThat(markCalls).isEqualTo(0);
        }

        void assertPublishedOnce() {
            assertThat(publishCalls).isEqualTo(1);
        }

        void assertNoPublish() {
            assertThat(publishCalls).isEqualTo(0);
        }
    }

}