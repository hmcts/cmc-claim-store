package uk.gov.hmcts.cmc.claimstore;

import org.flywaydb.core.Flyway;
import org.mockito.Answers;
import org.quartz.Scheduler;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;

import javax.sql.DataSource;

@Configuration
@Profile("mocked-database-tests")
@SuppressWarnings("unused")
class MockedDatabaseConfiguration {

    @MockBean
    private Flyway flyway;

    @MockBean(name = "dataSource", answer = Answers.RETURNS_MOCKS)
    private DataSource dataSource;

    @MockBean
    private ClaimRepository claimRepository;

    @MockBean
    private TestingSupportRepository testingSupportRepository;

    @MockBean
    private Scheduler testingScheduler;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        return new SchedulerFactoryBean() {
            @Override
            public Scheduler getScheduler() {
                return null;
            }
        };
    }

    @Bean
    protected PlatformTransactionManager transactionManager() {
        return new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
                return null;
            }

            @Override
            public void commit(TransactionStatus status) throws TransactionException {
                // NO-OP
            }

            @Override
            public void rollback(TransactionStatus status) throws TransactionException {
                // NO-OP
            }
        };
    }

}
