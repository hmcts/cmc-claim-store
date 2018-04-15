package uk.gov.hmcts.cmc.claimstore.config;

import org.skife.jdbi.v2.DBI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.cmc.claimstore.config.db.OptionalContainerFactory;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.OffersRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;

import javax.sql.DataSource;

@Configuration
public class DbConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .build();
    }

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSourceProxy(DataSource dataSource) {
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(TransactionAwareDataSourceProxy dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DBI dbi(TransactionAwareDataSourceProxy dataSource) {
        DBI dbi = new DBI(dataSource);
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

    @Bean
    public ReferenceNumberRepository referenceNumberRepository(DBI dbi) {
        return dbi.onDemand(ReferenceNumberRepository.class);
    }
}
