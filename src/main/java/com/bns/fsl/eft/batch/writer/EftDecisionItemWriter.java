package com.bns.fsl.eft.batch.writer;

import com.bns.fsl.eft.batch.model.EftDecisionResult;
import com.bns.fsl.eft.repository.IncomingPaymentStatusRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class EftDecisionItemWriter implements ItemWriter<EftDecisionResult> {

    private static final String UPDATED_BY = "FSL_RTP_EFT_BATCH";

    private final IncomingPaymentStatusRepository repository;

    public EftDecisionItemWriter(IncomingPaymentStatusRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends EftDecisionResult> chunk) {
        for (EftDecisionResult result : chunk) {
            repository.insertTerminalStatusIfAbsent(
                    result.source().getRequestId(),
                    result.source().getRequestSystem(),
                    result.terminalStatus(),
                    result.decision(),
                    result.source().getOriginalRequest(),
                    Instant.now(),
                    UPDATED_BY);
        }
    }
}
