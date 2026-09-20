package com.bns.fsl.eft.batch.processor;

import com.bns.fsl.eft.batch.EftDecisionBatchProperties;
import com.bns.fsl.eft.constants.EftProcessingStatus;
import com.bns.fsl.eft.model.EftDecisionResult;
import com.bns.fsl.eft.model.PendingEftDecisionProjection;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class EftDecisionItemProcessor
        implements ItemProcessor<PendingEftDecisionProjection, EftDecisionResult> {

    private final EftDecisionBatchProperties properties;
    private final Clock clock;

    public EftDecisionItemProcessor(EftDecisionBatchProperties properties) {
        this(properties, Clock.systemUTC());
    }

    EftDecisionItemProcessor(EftDecisionBatchProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public EftDecisionResult process(PendingEftDecisionProjection item) {
        if (item.getFraudDecision() != null && !item.getFraudDecision().isBlank()) {
            return new EftDecisionResult(
                    item, EftProcessingStatus.RESPONDED, item.getFraudDecision(), false);
        }

        Instant timeoutAt = item.getCreatedTs().plus(properties.autoApprovalTimeout());
        if (!clock.instant().isBefore(timeoutAt)) {
            return new EftDecisionResult(
                    item, EftProcessingStatus.AUTO_APPROVED, "APPROVED", true);
        }

        // Spring Batch uses null from ItemProcessor as an intentional filter:
        // this PENDING item is not ready for terminal processing in this execution.
        return null;
    }
}
