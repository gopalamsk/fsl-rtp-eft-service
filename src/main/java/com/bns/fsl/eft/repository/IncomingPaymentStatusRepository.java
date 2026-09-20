package com.bns.fsl.eft.repository;

import com.bns.fsl.eft.model.PendingEftDecisionProjection;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface IncomingPaymentStatusRepository extends Repository<Object, Long> {

    @Query("""
        SELECT TOP (:batchSize)
               ips.REQUEST_ID AS requestId,
               ips.REQUEST_SYSTEM AS requestSystem,
               ips.ORIGINAL_REQUEST AS originalRequest,
               ips.STATUS AS status,
               ips.CREATED_TS AS createdTs,
               fd.FRAUD_DECISION_ID AS fraudDecisionId,
               fd.FRAUD_SYSTEM AS fraudSystem,
               fd.FRAUD_CASE_ID AS fraudCaseId,
               fd.FRAUD_DECISION AS fraudDecision,
               fd.FRAUD_CHECK_SCORE AS fraudCheckScore,
               fd.ADDITIONAL_INFO1 AS fraudAdditionalInfo1,
               fd.ADDITIONAL_INFO2 AS fraudAdditionalInfo2
          FROM payment.INCOMING_PAYMENT_STATUS ips
          LEFT JOIN payment.FRAUD_DECISION fd
            ON fd.SOURCE_TRANSACTION_ID = ips.REQUEST_ID
         WHERE ips.STATUS = 'PENDING'
           AND NOT EXISTS (
               SELECT 1
                 FROM payment.INCOMING_PAYMENT_STATUS terminal
                WHERE terminal.REQUEST_ID = ips.REQUEST_ID
                  AND terminal.STATUS IN ('RESPONDED', 'AUTO_APPROVED')
           )
         ORDER BY ips.CREATED_TS ASC
        """)
    List<PendingEftDecisionProjection> findPendingRecords(@Param("batchSize") int batchSize);

    @Modifying
    @Query("""
        INSERT INTO payment.INCOMING_PAYMENT_STATUS
            (REQUEST_ID, REQUEST_SYSTEM, STATUS, DECISION, ORIGINAL_REQUEST,
             CREATED_TS, CREATED_BY)
        SELECT :requestId, :requestSystem, :status, :decision, :originalRequest,
               :createdTs, :createdBy
         WHERE NOT EXISTS (
               SELECT 1
                 FROM payment.INCOMING_PAYMENT_STATUS terminal
                WHERE terminal.REQUEST_ID = :requestId
                  AND terminal.STATUS IN ('RESPONDED', 'AUTO_APPROVED')
         )
        """)
    int insertTerminalStatusIfAbsent(
            @Param("requestId") Long requestId,
            @Param("requestSystem") String requestSystem,
            @Param("status") String status,
            @Param("decision") String decision,
            @Param("originalRequest") String originalRequest,
            @Param("createdTs") Instant createdTs,
            @Param("createdBy") String createdBy);
}
