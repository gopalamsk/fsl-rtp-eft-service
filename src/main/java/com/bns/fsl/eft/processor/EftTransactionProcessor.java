package com.bns.fsl.eft.processor;

import com.bns.fsl.eft.context.EftTransactionContext;
import com.bns.fsl.eft.handler.EftEventHandlerRouter;
import org.springframework.stereotype.Service;

@Service
public class EftTransactionProcessor {

    private final EftEventHandlerRouter handlerRouter;

    public EftTransactionProcessor(EftEventHandlerRouter handlerRouter) {
        this.handlerRouter = handlerRouter;
    }

    public EftTransactionContext process(EftTransactionContext context) {
        return handlerRouter.route(context);
    }
}
