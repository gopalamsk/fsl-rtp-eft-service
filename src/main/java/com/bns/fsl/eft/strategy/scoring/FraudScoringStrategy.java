package com.bns.fsl.eft.strategy.scoring;

import com.bns.fsl.eft.context.EftTransactionContext;

/**
 * Strategy contract for a fraud-scoring engine.
 *
 * Engine-specific mapping, transport and protocol details belong in the
 * concrete strategy implementation. Callers select an engine and scoring mode
 * without depending on ACI or any future scoring-engine implementation.
 */
public interface FraudScoringStrategy {

    FraudScoringEngine engine();

    boolean supports(FraudScoringMode mode);

    void submit(EftTransactionContext context, FraudScoringMode mode);
}
