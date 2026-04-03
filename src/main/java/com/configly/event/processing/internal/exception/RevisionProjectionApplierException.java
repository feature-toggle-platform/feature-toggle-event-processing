package com.configly.event.processing.internal.exception;

import com.configly.model.Revision;

public class RevisionProjectionApplierException extends RuntimeException {
    public RevisionProjectionApplierException(Revision incoming, Revision current) {
        super("Unhandled revision transition. Incoming revision: " + incoming.value() + ", current revision: " + current.value());
    }
}
