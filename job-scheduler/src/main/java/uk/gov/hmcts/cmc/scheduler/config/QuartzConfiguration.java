package uk.gov.hmcts.cmc.scheduler.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@ConfigurationProperties
public class QuartzConfiguration {

    private final Map<String, String> quartzProperties = new HashMap<>();

    // this getter is needed by the framework
    public Map<String, String> getQuartzProperties() {
        return quartzProperties;
    }

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        return jobFactory;
    }

    @Bean
    @DependsOn("flywayInitializer")
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
        Properties properties = new Properties();
        properties.putAll(quartzProperties);

        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setQuartzProperties(properties);

        return schedulerFactory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {

        Scheduler scheduler = factory.getScheduler();

        scheduler.start();

        return scheduler;
    }
}
