# EFT Pending Decision Batch — Implementation Guide

## Purpose

Use this document as implementation guidance when developing the EFT pending-decision Spring Batch flow in the company repository. The repository itself is the source of truth for existing infrastructure, contracts, schemas, generated classes, and coding conventions.

## Before Writing Code

Inspect the existing repository first. Reuse the project's established:

- package and naming conventions
- datasource and transaction-manager configuration
- Spring Batch infrastructure
- repositories and entities
- Kafka and MQ producers/services
- exception handling
- logging and monitoring
- generated request/response classes

Do not create duplicate infrastructure or invent contracts that cannot be verified from the repository.

Before implementation, identify:

1. Existing components that can be reused.
2. Proposed package/class structure.
3. Transaction boundary.
4. Behavior with chunk size 1.
5. Behavior with chunk size 10 when an item fails.
6. Idempotency strategy.
7. Failure, skip, and retry strategy.
8. Assumptions that cannot be verified from the repository.

## Business Flow

The scheduled job processes EFT payments currently in `PENDING` status.

1. Read at most the configured number of eligible pending payments.
2. Match each payment with fraud-decision data using the application's established transaction/correlation identifier.
3. If a fraud decision exists, produce a terminal `RESPONDED` result using that decision.
4. If no decision exists and the configured timeout has elapsed, produce `AUTO_APPROVED`.
5. If no decision exists and the timeout has not elapsed, leave the payment `PENDING`.
6. Persist terminal status according to the existing domain/database design.
7. Prevent duplicate terminal processing.

FOD auditing belongs to initial inbound processing and is not part of this pending-decision batch.

## Batch Structure

Use chunk-oriented Spring Batch processing with clear separation:

```text
Scheduler
  -> Job
     -> Step
        -> ItemReader
        -> ItemProcessor
        -> ItemWriter
```

Keep batch-specific classes under the `batch` package, organized where appropriate into:

```text
batch/
├── config/
├── reader/
├── processor/
├── writer/
├── listener/
├── exception/
└── model/
```

Shared repositories, entities, producers, and services should remain in their normal application packages rather than being moved into `batch`.

## Reader

The reader should:

- be an explicit ItemReader component
- select only eligible `PENDING` payments
- exclude payments that already have a terminal result
- use deterministic ordering
- respect the configured batch size
- use existing repository/datasource conventions
- avoid database locking hints unless required by the approved concurrency design

## Processor

Keep decision logic as pure as practical.

Decision rules:

```text
Fraud decision exists
    -> RESPONDED

No fraud decision + timeout reached
    -> AUTO_APPROVED

No fraud decision + timeout not reached
    -> no terminal result in this execution
```

The processor should not perform Kafka or MQ calls.

Validate required fields and use specific exceptions for known invalid business/data conditions.

## Writer

The writer must be safe for both one-item and multi-item chunks.

- Make terminal persistence idempotent.
- Never assume the chunk contains exactly one item.
- Preserve the Spring Batch transaction boundary.
- Do not swallow persistence/database exceptions.
- Assume an item can be processed again after rollback or restart.

## Chunk and Transaction Strategy

Make chunk size configurable.

Initial setting:

```text
batch-size = 500
chunk-size = 1
```

Start with `chunk-size=1` for strong per-payment failure isolation. Measure transaction/commit overhead in realistic environments.

If measurements justify it, change configuration later:

```text
chunk-size = 10
```

Correctness must not depend on chunk size being 1.

With chunk size 1, each successful payment commits independently.

With chunk size 10, items in the chunk share a transaction. An unexpected database/infrastructure failure can roll back the entire chunk and cause previously processed items in that chunk to be processed again. Therefore all persistent and downstream processing must be designed for idempotency.

Changing chunk size should be an operational/configuration change, not require rewriting the reader, processor, or writer.

## Failure Handling

Differentiate business/data problems from system failures.

- Do not configure broad `.skip(Exception.class)`.
- Only explicitly approved business/data exceptions should be skippable.
- Database outages, connectivity failures, programming defects, and unexpected runtime failures should fail the step/job.
- Do not catch and swallow infrastructure exceptions merely to force the chunk to commit.
- Prefer Spring Batch fault-tolerance mechanisms over custom manual loops.
- If retry is introduced, retry only explicitly approved transient failures with bounded attempts.
- Log identifiers and operational context, not sensitive payment/customer payloads.
- Add useful metrics for batch execution and approved skip/retry behavior.

## Idempotency

Assume processing can be repeated because of:

- Kafka at-least-once delivery elsewhere in the flow
- batch restart
- chunk rollback
- scheduler/job retry
- application restart

Terminal writes must therefore detect an already-completed payment and avoid producing duplicate terminal processing.

Do not rely only on `chunk-size=1` as an idempotency mechanism.

## Scheduling and ShedLock

Use the application's existing scheduling infrastructure.

If ShedLock is the approved distributed scheduler-lock mechanism:

- acquire one distributed lock around scheduled job launch
- use the existing application datasource
- use database time when that is the established convention
- configure sensible lock-at-most and lock-at-least durations
- do not create the ShedLock table from application startup code
- create/manage schema through the organization's approved database migration/deployment process

Keep Spring Batch metadata datasource and transaction-manager wiring aligned with the existing project.

## External Messaging

PHUB response delivery and PRM/ACI NRT submission have different business criticality and must follow the application's approved reliability patterns.

Do not treat database + Kafka + MQ as automatically atomic.

Before implementing outbound delivery, inspect existing company patterns for:

- transactional outbox
- delivery-state tables
- Kafka retry/recovery
- MQ retry/recovery
- idempotent producer behavior

Reuse existing producers and contracts. Do not invent Kafka/MQ APIs, schemas, generated classes, table columns, or statuses.

Critical PHUB delivery should have an approved durable recovery mechanism. NRT behavior should follow its documented business criticality.

## Configuration

Operational values should be externalized, including:

```yaml
eft:
  batch:
    pending-decision:
      cron: ...
      batch-size: 500
      chunk-size: 1
      auto-approval-timeout: ...
      transaction-timeout: ...
      skip-limit: ...
```

Do not hard-code values that operations may need to tune.

## Recommended Implementation Order

Implement and review incrementally:

1. Inspect existing infrastructure and conventions.
2. Confirm database/query and correlation-key requirements.
3. Implement reader/query.
4. Implement pure decision processor.
5. Implement idempotent terminal writer.
6. Configure job and step with configurable chunk size.
7. Configure scheduler and distributed locking.
8. Add selective failure/skip/retry handling.
9. Add unit and integration tests.
10. Integrate outbound PHUB/NRT behavior using existing approved enterprise patterns.

Do not accept or generate a large implementation before validating each layer against the actual repository.

## Production Review Checklist

Before considering the job production-ready, verify:

- chunk size 1 behavior
- chunk size 10 behavior
- rollback and reprocessing behavior
- terminal-write idempotency
- duplicate job-launch protection
- restart behavior
- malformed item behavior
- database outage behavior
- timeout boundary behavior
- no-decision/not-timed-out filtering
- concurrent execution assumptions
- transaction manager and datasource selection
- Spring Batch metadata configuration
- PHUB delivery recovery
- NRT failure behavior
- logging contains no sensitive payloads
- operational metrics/alerts are available
- configuration can be tuned without code changes
- integration tests exercise the real SQL dialect and repository queries

## Copilot Guidance

When using IntelliJ Copilot, instruct it to inspect the repository before generating code. It should explain what existing components it will reuse and identify unverifiable assumptions before implementation.

Do not allow Copilot to generate placeholder/TODO/sample production code or invent schemas, generated classes, MQ/Kafka APIs, statuses, or infrastructure that cannot be found in the repository.
