package com.bns.fsl.eft.model;

public record EftDecisionResult(
        PendingEftDecisionProjection source,
        String terminalStatus,
        String decision,
        boolean autoApproved) {
}
