package com.bns.fsl.eft.batch.reader;

import com.bns.fsl.eft.model.PendingEftDecisionProjection;
import org.springframework.batch.item.ItemReader;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public final class EftDecisionItemReader implements ItemReader<PendingEftDecisionProjection> {

    private final Supplier<List<PendingEftDecisionProjection>> loader;
    private Iterator<PendingEftDecisionProjection> iterator;

    public EftDecisionItemReader(Supplier<List<PendingEftDecisionProjection>> loader) {
        this.loader = loader;
    }

    @Override
    public PendingEftDecisionProjection read() {
        if (iterator == null) {
            iterator = loader.get().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}
