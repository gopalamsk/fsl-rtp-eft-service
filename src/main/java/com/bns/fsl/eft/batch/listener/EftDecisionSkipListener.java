package com.bns.fsl.eft.batch.listener;

import com.bns.fsl.eft.batch.model.EftDecisionResult;
import com.bns.fsl.eft.batch.model.PendingEftDecisionProjection;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class EftDecisionSkipListener
        implements SkipListener<PendingEftDecisionProjection, EftDecisionResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EftDecisionSkipListener.class);

    private final Counter readSkipCounter;
    private final Counter processSkipCounter;
    private final Counter writeSkipCounter;

    public EftDecisionSkipListener(MeterRegistry meterRegistry) {
        this.readSkipCounter = counter(meterRegistry, "read");
        this.processSkipCounter = counter(meterRegistry, "process");
        this.writeSkipCounter = counter(meterRegistry, "write");
    }

    private static Counter counter(MeterRegistry registry, String phase) {
        return Counter.builder("eft.batch.decision.skipped")
                .description("EFT decision reconciliation items skipped by Spring Batch")
                .tag("phase", phase)
                .register(registry);
    }

    @Override
    public void onSkipInRead(Throwable throwable) {
        readSkipCounter.increment();
        LOGGER.error("Skipped EFT reconciliation item during read", throwable);
    }

    @Override
    public void onSkipInProcess(PendingEftDecisionProjection item, Throwable throwable) {
        processSkipCounter.increment();
        LOGGER.error("Skipped EFT reconciliation item during processing. requestId={}",
                item == null ? null : item.getRequestId(), throwable);
    }

    @Override
    public void onSkipInWrite(EftDecisionResult item, Throwable throwable) {
        writeSkipCounter.increment();
        Long requestId = item == null || item.source() == null ? null : item.source().getRequestId();
        LOGGER.error("Skipped EFT reconciliation item during write. requestId={}", requestId, throwable);
    }
}
