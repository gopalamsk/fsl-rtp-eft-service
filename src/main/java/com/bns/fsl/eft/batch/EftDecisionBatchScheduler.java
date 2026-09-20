package com.bns.fsl.eft.batch;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EftDecisionBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job reconciliationJob;

    public EftDecisionBatchScheduler(
            JobLauncher jobLauncher,
            @Qualifier("eftDecisionReconciliationJob") Job reconciliationJob) {
        this.jobLauncher = jobLauncher;
        this.reconciliationJob = reconciliationJob;
    }

    @Scheduled(cron = "${eft.batch.decision-reconciliation.cron:0 */5 * * * *}")
    @SchedulerLock(
            name = "eftDecisionReconciliationJob",
            lockAtMostFor = "PT1H",
            lockAtLeastFor = "PT10S")
    public void launch() throws Exception {
        jobLauncher.run(
                reconciliationJob,
                new JobParametersBuilder()
                        .addLong("scheduledAt", System.currentTimeMillis())
                        .toJobParameters());
    }
}
