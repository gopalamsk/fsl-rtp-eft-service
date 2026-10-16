package com.bns.fsl.eft.batch.exception;

public class EftInvalidBatchItemException extends RuntimeException {

    private final Long requestId;

    public EftInvalidBatchItemException(Long requestId, String message) {
        super(message);
        this.requestId = requestId;
    }

    public Long getRequestId() {
        return requestId;
    }
}
