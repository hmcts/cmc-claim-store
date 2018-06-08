package uk.gov.hmcts.cmc.scheduler.config;

import org.flywaydb.core.Flyway;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;

@Configuration
@ConfigurationProperties
public class QuartzConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<String, String> quartzProperties = new HashMap<>();

    // this getter is needed by the framework
    public Map<String, String> getQuartzProperties() {
        return quartzProperties;
    }

    @Bean
    @ConfigurationProperties("spring.datasource.scheduler")
    public DataSourceProperties schedulerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("schedulerDataSource")
    @ConfigurationProperties("spring.datasource.scheduler")
    public DataSource schedulerDataSource() {
        migrateFlyway(schedulerDataSourceProperties());
        return schedulerDataSourceProperties().initializeDataSourceBuilder().build();
    }

    private void migrateFlyway(DataSourceProperties flywayProperties) {
        final Flyway flyway = new Flyway();
        flyway.setDataSource(
            flywayProperties.getUrl(),
            flywayProperties.getUsername(),
            flywayProperties.getPassword());
        flyway.setLocations("scheduler/db/migration");
        flyway.migrate();
    }

    @Bean
    public TransactionAwareDataSourceProxy schedulerTransactionAwareDataSourceProxy(
        @Qualifier("schedulerDataSource") DataSource dataSource
    ) {
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Bean
    public PlatformTransactionManager schedulerTransactionManager(
        TransactionAwareDataSourceProxy schedulerTransactionAwareDataSourceProxy
    ) {
        return new DataSourceTransactionManager(schedulerTransactionAwareDataSourceProxy);
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
        TransactionAwareDataSourceProxy schedulerTransactionAwareDataSourceProxy,
        PlatformTransactionManager schedulerTransactionManager
    ) {
        Properties properties = new Properties();
        properties.putAll(quartzProperties);

        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setDataSource(schedulerTransactionAwareDataSourceProxy);
        schedulerFactory.setTransactionManager(schedulerTransactionManager);
        schedulerFactory.setQuartzProperties(properties);
        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setSchedulerName("CMC Job Scheduler");

        return schedulerFactory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {

        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        scheduler.start();

        return scheduler;
    }
}
