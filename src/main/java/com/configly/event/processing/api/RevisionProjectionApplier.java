package com.configly.event.processing.api;

import com.configly.event.processing.internal.RevisionApplierResult;

public interface RevisionProjectionApplier {

    <T> RevisionApplierResult apply(RevisionProjectionPlan<T> plan);

}
