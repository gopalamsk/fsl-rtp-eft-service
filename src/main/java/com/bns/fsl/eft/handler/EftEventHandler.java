package com.bns.fsl.eft.handler;

import com.bns.fsl.eft.context.EftTransactionContext;

public interface EftEventHandler {
    EftEventAction supportedAction();
    EftTransactionContext handle(EftTransactionContext context);
}
