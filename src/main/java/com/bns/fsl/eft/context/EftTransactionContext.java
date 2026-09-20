package com.bns.fsl.eft.context;

public record EftTransactionContext(
        String transactionId,
        String correlationId,
        String eventAction,
        String originalPayload,
        String processingStatus) {

    public EftTransactionContext withProcessingStatus(String status) {
        return new EftTransactionContext(
                transactionId, correlationId, eventAction, originalPayload, status);
    }
}
