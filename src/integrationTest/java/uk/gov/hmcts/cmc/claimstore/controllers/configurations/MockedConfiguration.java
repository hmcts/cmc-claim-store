package uk.gov.hmcts.cmc.claimstore.controllers.configurations;

import org.flywaydb.core.Flyway;
import org.mockito.Answers;
import org.skife.jdbi.v2.DBI;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import javax.sql.DataSource;

@Configuration
public class MockedConfiguration {

    @MockBean(name = "dataSource", answer = Answers.RETURNS_MOCKS)
    public DataSource dataSource;

    @MockBean
    public DBI dbi;

    @MockBean
    public Flyway flyway;

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new MockedTransactionManager();
    }

    public static class MockedTransactionManager implements PlatformTransactionManager {

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            return null;
        }

        @Override
        public void commit(TransactionStatus status) throws TransactionException {

        }

        @Override
        public void rollback(TransactionStatus status) throws TransactionException {

        }
    }

}
