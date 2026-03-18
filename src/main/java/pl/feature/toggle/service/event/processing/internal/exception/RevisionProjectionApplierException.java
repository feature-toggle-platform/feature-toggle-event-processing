package pl.feature.toggle.service.event.processing.internal.exception;

import pl.feature.toggle.service.model.Revision;

public class RevisionProjectionApplierException extends RuntimeException {
    public RevisionProjectionApplierException(Revision incoming, Revision current) {
        super("Unhandled revision transition. Incoming revision: " + incoming.value() + ", current revision: " + current.value());
    }
}
