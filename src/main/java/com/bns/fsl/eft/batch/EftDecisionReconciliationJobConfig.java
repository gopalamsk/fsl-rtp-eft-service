package com.bns.fsl.eft.batch;

import com.bns.fsl.eft.batch.processor.EftDecisionItemProcessor;
import com.bns.fsl.eft.batch.writer.EftDecisionItemWriter;
import com.bns.fsl.eft.model.EftDecisionResult;
import com.bns.fsl.eft.model.PendingEftDecisionProjection;
import com.bns.fsl.eft.repository.IncomingPaymentStatusRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Configuration
@EnableConfigurationProperties(EftDecisionBatchProperties.class)
public class EftDecisionReconciliationJobConfig {

    @Bean
    @Scope("step")
    ItemReader<PendingEftDecisionProjection> eftDecisionItemReader(
            IncomingPaymentStatusRepository repository,
            EftDecisionBatchProperties properties) {
        return new ListItemReader<>(repository.findPendingRecords(properties.batchSize()));
    }

    @Bean
    Step eftDecisionReconciliationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<PendingEftDecisionProjection> eftDecisionItemReader,
            EftDecisionItemProcessor processor,
            EftDecisionItemWriter writer,
            EftDecisionBatchProperties properties) {

        DefaultTransactionAttribute tx = new DefaultTransactionAttribute();
        tx.setTimeout((int) properties.transactionTimeout().toSeconds());

        return new StepBuilder("eftDecisionReconciliationStep", jobRepository)
                .<PendingEftDecisionProjection, EftDecisionResult>chunk(1, transactionManager)
                .reader(eftDecisionItemReader)
                .processor(processor)
                .writer(writer)
                .transactionAttribute(tx)
                .build();
    }

    @Bean
    Job eftDecisionReconciliationJob(
            JobRepository jobRepository,
            @Qualifier("eftDecisionReconciliationStep") Step step) {
        return new JobBuilder("eftDecisionReconciliationJob", jobRepository)
                .start(step)
                .build();
    }
}
