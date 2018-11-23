package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDCaseHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDTestingSupportHandler;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.support.CCDTestingSupportRepository;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Configuration
@EnableRetry
@EnableAsync
public class CCDStoreConfiguration {

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(50);
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnProperty(prefix = "feature_toggles", name = "ccd_parallel_run", havingValue = "true")
    public CCDCaseHandler ccdCaseHandler(
        CCDCaseRepository ccdCaseRepository,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator
    ) {
        return new CCDCaseHandler(ccdCaseRepository, directionsQuestionnaireDeadlineCalculator);
    }

    @Bean
    @ConditionalOnProperty("claim-store.test-support.enabled")
    public CCDTestingSupportHandler ccdTestingSupportHandler(
        CCDTestingSupportRepository ccdSupportRepository
    ) {
        return new CCDTestingSupportHandler(ccdSupportRepository);
    }
}
