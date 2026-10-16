# fsl-rtp-eft-service architecture

Runtime baseline: Java 21 + Spring Boot 4.x.

Canonical package root:

```text
com.bns.fsl.eft
├── batch/
│   ├── reader/
│   ├── processor/
│   └── writer/
├── config/
├── constants/
├── consumer/
├── context/
├── entity/
├── exception/
├── handler/
├── mapper/
├── model/
├── outbox/
├── processor/
├── repository/
├── service/
└── util/
```

## Inbound architecture

PHUB -> EFT Request Kafka -> EftInboundKafkaConsumer -> EftTransactionProcessor -> EftEventHandlerRouter.

The handler package implements Strategy + Router:

- ONE_TIME -> OneTimePaymentHandler -> RT fraud flow.
- FILE_UPLOAD -> FileUploadHandler -> NRT flow.
- STATUS_MESSAGE -> StatusMessageHandler -> NRT flow.

ONE_TIME is asynchronous. The inbound Kafka consumer must not wait for the fraud-decision response.

## Reconciliation

A separate fraud-decision component persists PRM/ACI decisions in payment.FRAUD_DECISION.

A ShedLock-protected Spring Batch job reconciles PENDING transactions. The target implementation is chunk-oriented with commit interval 1 so each payment has an independent transaction boundary.

The pending query intentionally does not use UPDLOCK/READPAST because ShedLock serializes scheduled reconciliation.

The batch writer must not directly publish PHUB/NRT messages. Terminal state and durable outbox events are persisted atomically; outbox publishers perform external delivery afterward.

## Delivery semantics

The service is designed for at-least-once processing. Database constraints provide authoritative idempotency protection. Do not claim exactly-once delivery.
