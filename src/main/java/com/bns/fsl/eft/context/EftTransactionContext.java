package com.bns.fsl.eft.context;

import com.bns.fsl.schema.eft.model.EftFraudPaymentRequest;
import com.bns.fsl.schema.eft.model.EftFraudPaymentResponse;
import com.bns.fsl.schema.rtp.aci.model.XFRqst;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Short-lived processing context carried through the EFT inbound flow.
 *
 * This preserves the existing application model instead of introducing a
 * replacement transport/domain DTO. Generated PHUB and ACI models remain the
 * canonical request/response contracts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EftTransactionContext {

    private String transactionId;
    private String correlationId;
    private String eventAction;

    private EftFraudPaymentRequest phubPaymentRequest;
    private XFRqst aciRTPaymentRequest;
    private XFRqst aciNRTPaymentRequest;
    private EftFraudPaymentResponse phubPaymentResponse;

    private String processingStatus;
    private String errorMessage;
}
