package com.bns.fsl.eft.batch.processor;

import com.bns.fsl.eft.batch.config.EftDecisionBatchProperties;
import com.bns.fsl.eft.batch.exception.EftInvalidBatchItemException;
import com.bns.fsl.eft.batch.model.EftDecisionResult;
import com.bns.fsl.eft.batch.model.PendingEftDecisionProjection;
import com.bns.fsl.eft.constants.EftProcessingStatus;
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
        validate(item);

        if (item.getFraudDecision() != null && !item.getFraudDecision().isBlank()) {
            return new EftDecisionResult(
                    item, EftProcessingStatus.RESPONDED, item.getFraudDecision(), false);
        }

        Instant timeoutAt = item.getCreatedTs().plus(properties.autoApprovalTimeout());
        if (!clock.instant().isBefore(timeoutAt)) {
            return new EftDecisionResult(
                    item, EftProcessingStatus.AUTO_APPROVED, "APPROVED", true);
        }

        return null;
    }

    private static void validate(PendingEftDecisionProjection item) {
        if (item == null) throw new EftInvalidBatchItemException(null, "Pending EFT decision item is null");
        if (item.getRequestId() == null) throw new EftInvalidBatchItemException(null, "requestId is required");
        if (item.getCreatedTs() == null) throw new EftInvalidBatchItemException(item.getRequestId(), "createdTs is required");
        if (item.getRequestSystem() == null || item.getRequestSystem().isBlank())
            throw new EftInvalidBatchItemException(item.getRequestId(), "requestSystem is required");
        if (item.getOriginalRequest() == null || item.getOriginalRequest().isBlank())
            throw new EftInvalidBatchItemException(item.getRequestId(), "originalRequest is required");
    }
}
