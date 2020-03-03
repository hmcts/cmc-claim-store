package uk.gov.hmcts.cmc.claimstore.tests.deprectaed;

import org.flywaydb.core.Flyway;
import org.mockito.Answers;
import org.quartz.Scheduler;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;

import javax.sql.DataSource;

@Configuration
@Profile("mocked-database-tests")
@SuppressWarnings({"unused", "NullableProblems"})
class MockedDatabaseConfiguration {

    private static final PlatformTransactionManager NO_OP_TRANSACTION_MANAGER = new PlatformTransactionManager() {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            return null;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void commit(TransactionStatus status) throws TransactionException {
            // NO-OP
        }

        @Override
        public void rollback(TransactionStatus status) throws TransactionException {
            // NO-OP
        }
    };

    @MockBean
    private Flyway flyway;

    @MockBean
    private ClaimRepository claimRepository;

    @MockBean
    private TestingSupportRepository testingSupportRepository;

    @MockBean(name = "dataSource", answer = Answers.RETURNS_MOCKS)
    private DataSource dataSource;

    @MockBean
    private SpringBeanJobFactory springBeanJobFactory;

    @MockBean
    private SchedulerFactoryBean schedulerFactoryBean;

    @MockBean
    private Scheduler scheduler;

    @MockBean(name = "transactionAwareDataSourceProxy")
    private TransactionAwareDataSourceProxy transactionAwareDataSourceProxy;

    @MockBean(name = "transactionManager")
    private PlatformTransactionManager transactionManager;

    @Bean
    protected PlatformTransactionManager transactionManager() {
        return NO_OP_TRANSACTION_MANAGER;
    }
}
