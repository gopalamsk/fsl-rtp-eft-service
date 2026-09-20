package com.bns.fsl.eft.handler;

import com.bns.fsl.eft.context.EftTransactionContext;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class EftEventHandlerRouter {

    private final Map<EftEventAction, EftEventHandler> handlers;

    public EftEventHandlerRouter(List<EftEventHandler> handlers) {
        this.handlers = new EnumMap<>(EftEventAction.class);
        for (EftEventHandler handler : handlers) {
            EftEventHandler previous = this.handlers.put(handler.supportedAction(), handler);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate EFT event handler for action: " + handler.supportedAction());
            }
        }
    }

    public EftTransactionContext route(EftTransactionContext context) {
        if (context.eventAction() == null || context.eventAction().isBlank()) {
            throw new IllegalArgumentException("metadata.event_action is missing");
        }

        final EftEventAction action;
        try {
            action = EftEventAction.valueOf(
                    context.eventAction().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported metadata.event_action: " + context.eventAction(), exception);
        }

        EftEventHandler handler = handlers.get(action);
        if (handler == null) {
            throw new IllegalStateException("No EFT handler registered for action: " + action);
        }
        return handler.handle(context);
    }
}
