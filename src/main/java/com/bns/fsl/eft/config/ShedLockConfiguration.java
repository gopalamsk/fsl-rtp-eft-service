package com.bns.fsl.eft.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT1H")
public class ShedLockConfiguration {

    @Bean
    LockProvider lockProvider(
            @Qualifier("fslCacheDataSource") DataSource fslCacheDataSource) {

        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(fslCacheDataSource))
                        .usingDbTime()
                        .withTableName("payment.shedlock")
                        .build());
    }
}
