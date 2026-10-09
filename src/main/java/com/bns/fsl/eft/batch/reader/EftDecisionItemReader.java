package com.bns.fsl.eft.batch.reader;

import com.bns.fsl.eft.batch.config.EftDecisionBatchProperties;
import com.bns.fsl.eft.batch.model.PendingEftDecisionProjection;
import com.bns.fsl.eft.repository.IncomingPaymentStatusRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("step")
public class EftDecisionItemReader implements ItemReader<PendingEftDecisionProjection> {

    private final ListItemReader<PendingEftDecisionProjection> delegate;

    public EftDecisionItemReader(
            IncomingPaymentStatusRepository repository,
            EftDecisionBatchProperties properties) {
        this.delegate = new ListItemReader<>(repository.findPendingRecords(properties.batchSize()));
    }

    @Override
    public PendingEftDecisionProjection read() {
        return delegate.read();
    }
}
