package com.bns.fsl.eft.batch.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "eft.batch.decision-reconciliation")
public record EftDecisionBatchProperties(
        @Min(1) int batchSize,
        @NotNull Duration autoApprovalTimeout,
        @NotNull Duration transactionTimeout,
        @Min(1) int skipLimit) {

    public EftDecisionBatchProperties {
        if (batchSize == 0) batchSize = 500;
        if (autoApprovalTimeout == null) autoApprovalTimeout = Duration.ofHours(4);
        if (transactionTimeout == null) transactionTimeout = Duration.ofSeconds(30);
        if (skipLimit == 0) skipLimit = 10;
    }
}
