package com.bns.fsl.eft.batch;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EftPendingDecisionJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job pendingDecisionProcessorJob;

    public EftPendingDecisionJobScheduler(
            JobLauncher jobLauncher,
            @Qualifier("eftPendingDecisionProcessorJob") Job pendingDecisionProcessorJob) {
        this.jobLauncher = jobLauncher;
        this.pendingDecisionProcessorJob = pendingDecisionProcessorJob;
    }

    @Scheduled(cron = "${eft.batch.pending-decision.cron:0 */5 * * * *}")
    @SchedulerLock(
            name = "eftPendingDecisionProcessorJob",
            lockAtMostFor = "PT1H",
            lockAtLeastFor = "PT10S")
    public void launch() throws Exception {
        jobLauncher.run(
                pendingDecisionProcessorJob,
                new JobParametersBuilder()
                        .addLong("scheduledAt", System.currentTimeMillis())
                        .toJobParameters());
    }
}
