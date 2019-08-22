package uk.gov.hmcts.cmc.claimstore.config;

import org.skife.jdbi.v2.DBI;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.cmc.claimstore.config.db.OptionalContainerFactory;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.OffersRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;

import javax.sql.DataSource;

@Configuration
public class DbConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.claimstore")
    public DataSourceProperties claimStoreDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.claimstore")
    public DataSource claimStoreDataSource() {
        return claimStoreDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSourceProxy(DataSource claimStoreDataSource) {
        return new TransactionAwareDataSourceProxy(claimStoreDataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(
        TransactionAwareDataSourceProxy transactionAwareDataSourceProxy
    ) {
        return new DataSourceTransactionManager(transactionAwareDataSourceProxy);
    }

    @Bean
    public DBI dbi(TransactionAwareDataSourceProxy transactionAwareDataSourceProxy) {
        DBI dbi = new DBI(transactionAwareDataSourceProxy);
        dbi.registerContainerFactory(new OptionalContainerFactory());

        return dbi;
    }

    @Bean
    public ClaimRepository claimRepository(DBI dbi) {
        return dbi.onDemand(ClaimRepository.class);
    }

    @Bean
    public TestingSupportRepository testingSupportRepository(DBI dbi) {
        return dbi.onDemand(TestingSupportRepository.class);
    }

    @Bean
    public OffersRepository offersRepository(DBI dbi) {
        return dbi.onDemand(OffersRepository.class);
    }
}
