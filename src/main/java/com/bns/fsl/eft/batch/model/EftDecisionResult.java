package com.bns.fsl.eft.batch.model;

public record EftDecisionResult(
        PendingEftDecisionProjection source,
        String terminalStatus,
        String decision,
        boolean autoApproved) {
}
