package com.bns.fsl.eft.strategy.scoring;

import com.bns.fsl.eft.context.EftTransactionContext;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class FraudScoringStrategyRouter {

    private final Map<FraudScoringEngine, FraudScoringStrategy> strategies;

    public FraudScoringStrategyRouter(List<FraudScoringStrategy> strategies) {
        Map<FraudScoringEngine, FraudScoringStrategy> registered =
                new EnumMap<>(FraudScoringEngine.class);

        for (FraudScoringStrategy strategy : strategies) {
            FraudScoringEngine engine =
                    Objects.requireNonNull(strategy.engine(), "Fraud scoring engine is required");

            FraudScoringStrategy previous = registered.put(engine, strategy);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate fraud scoring strategy registered for engine: " + engine);
            }
        }

        this.strategies = Map.copyOf(registered);
    }

    public void submit(
            FraudScoringEngine engine,
            FraudScoringMode mode,
            EftTransactionContext context) {

        Objects.requireNonNull(engine, "Fraud scoring engine is required");
        Objects.requireNonNull(mode, "Fraud scoring mode is required");
        Objects.requireNonNull(context, "EFT transaction context is required");

        FraudScoringStrategy strategy = strategies.get(engine);
        if (strategy == null) {
            throw new IllegalStateException(
                    "No fraud scoring strategy registered for engine: " + engine);
        }

        if (!strategy.supports(mode)) {
            throw new IllegalStateException(
                    "Fraud scoring engine " + engine + " does not support mode " + mode);
        }

        strategy.submit(context, mode);
    }
}
