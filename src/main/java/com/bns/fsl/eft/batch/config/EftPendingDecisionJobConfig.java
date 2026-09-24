package com.bns.fsl.eft.batch.config;

import com.bns.fsl.eft.batch.exception.EftInvalidBatchItemException;
import com.bns.fsl.eft.batch.listener.EftDecisionSkipListener;
import com.bns.fsl.eft.batch.model.EftDecisionResult;
import com.bns.fsl.eft.batch.model.PendingEftDecisionProjection;
import com.bns.fsl.eft.batch.processor.EftDecisionItemProcessor;
import com.bns.fsl.eft.batch.reader.EftDecisionItemReader;
import com.bns.fsl.eft.batch.writer.EftDecisionItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Configuration
@EnableConfigurationProperties(EftDecisionBatchProperties.class)
public class EftPendingDecisionJobConfig {

    @Bean
    Step eftPendingDecisionProcessorStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            EftDecisionItemReader reader,
            EftDecisionItemProcessor processor,
            EftDecisionItemWriter writer,
            EftDecisionSkipListener skipListener,
            EftDecisionBatchProperties properties) {

        DefaultTransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout((int) properties.transactionTimeout().toSeconds());

        return new StepBuilder("eftPendingDecisionProcessorStep", jobRepository)
                .<PendingEftDecisionProjection, EftDecisionResult>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(EftInvalidBatchItemException.class)
                .skipLimit(properties.skipLimit())
                .listener(skipListener)
                .transactionAttribute(transactionAttribute)
                .build();
    }

    @Bean
    Job eftPendingDecisionProcessorJob(
            JobRepository jobRepository,
            @Qualifier("eftPendingDecisionProcessorStep") Step step) {
        return new JobBuilder("eftPendingDecisionProcessorJob", jobRepository)
                .start(step)
                .build();
    }
}
