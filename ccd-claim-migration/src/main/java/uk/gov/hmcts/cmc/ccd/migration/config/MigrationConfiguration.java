package uk.gov.hmcts.cmc.ccd.migration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableRetry
@EnableAsync
public class MigrationConfiguration {
    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor(@Value("${async.max.threadPool.size}") Integer maxThreadPoolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxThreadPoolSize);
        executor.initialize();
        return executor;
    }
}
