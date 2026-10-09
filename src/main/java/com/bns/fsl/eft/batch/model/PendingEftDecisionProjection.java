package com.bns.fsl.eft.batch.model;

import java.time.Instant;

public interface PendingEftDecisionProjection {
    Long getRequestId();
    String getRequestSystem();
    String getOriginalRequest();
    String getStatus();
    Instant getCreatedTs();
    Long getFraudDecisionId();
    String getFraudSystem();
    String getFraudCaseId();
    String getFraudDecision();
    String getFraudCheckScore();
    String getFraudAdditionalInfo1();
    String getFraudAdditionalInfo2();
}
