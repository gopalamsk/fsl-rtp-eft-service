# fsl-rtp-eft-service

Java 21 / Spring Boot 4 EFT fraud-processing service.

Initial repository bootstrap. Production baseline is developed through feature branches and pull requests.
Here’s a complete reply you can paste into Copilot, including the updated schedule and 20-minute deadline.

Please use these requirements to prepare the design:

1. Standards
    I am not sure whether our team mandates Spring Batch. Our earlier design used Spring Batch with commit interval 1 and ShedLock. Review that approach against a simple scheduled job and explain which fits these requirements better before changing it.
2. Trigger and schedule
    The cron schedule will eventually be configured in the database. For now, read it from application.yml. Keep the configuration source easy to replace. The final schedule is still to be decided; every minute is the current example.
3. Decision source
    The decision comes from PAYMENT.FRAUD_DECISION, populated by another service. Correlate it with INCOMING_PAYMENT_STATUS using the agreed transaction identifier. Verify the actual columns and identifier mapping before implementing the query.
4. Response expectation
    PHUB automatically approves the payment if it receives no response within 20 minutes. Our response must reach PHUB before that deadline. Calculate payment age using the timestamp aligned with PHUB’s timer, not the time the job reads the record.
5. No decision yet
    If no decision exists, leave the payment PENDING until the configured fallback threshold.

For a one-minute polling interval, propose an 18-minute threshold to leave time for processing, publishing, and retries. At that threshold, publish the agreed auto-approval response and mark AUTO_APPROVED after publishing succeeds.

Confirm the fallback response value and handling of payments already past the 20-minute deadline. Check again for an available decision before choosing auto-approval.

6. Scope
    This polling flow applies only to RT one-time payments. FILE_UPLOAD and STATUS_MESSAGE follow the NRT flow. Verify the actual RT event-action value in the code because the questions use ONLINE_PAYMENT while the earlier design used ONE_TIME.
7. Status tracking
    Use INCOMING_PAYMENT_STATUS:

* PENDING → RESPONDED after successfully publishing an available decision.
* PENDING → AUTO_APPROVED after successfully publishing the timeout response.

Exclude completed records from later processing. Prevent stale processing from overwriting a terminal status.

8. Duplicates
    Design to avoid resending successfully handled payments. PHUB’s duplicate-handling contract still needs confirmation.

Explicitly address the case where publishing succeeds but the database update fails. ShedLock and a per-item database transaction alone must not be presented as a guarantee against duplicate delivery.

9. Multiple pods
    Support multiple application instances using a shared ShedLock table and the same lock name. The DBA is handling table creation; confirm its availability and approved column names.

Keep the lock active for the complete processing run, not just while launching the job. Keep lock durations configurable.

10. Audit and NRT
    FOD auditing is required for all ingestion event actions. Confirm whether decision responses also require FOD auditing.

The earlier design includes an NRT follow-up after the decision. Confirm its destination and applicable cases. An NRT failure must not trigger another PHUB response after PHUB publishing has succeeded.

11. Failures
    Use bounded retries for transient PHUB publishing failures. If publishing fails, do not mark the payment RESPONDED or AUTO_APPROVED.

Continue processing other records when one fails. Avoid unlimited retries for permanent mapping or validation errors. Propose an attempt limit, alerting, and a recovery path that accounts for the 20-minute deadline.

12. Naming and location
    Keep job-related code and configuration under /batch. EftDecisionSweepJob is acceptable.

Review and reuse the files already created where appropriate. Identify necessary changes before deleting or replacing anything.

Prepare the design first, including processing flow, transaction boundaries, duplicate handling, required tables, and unresolved assumptions. Keep it simple and avoid unnecessary abstractions.