package uk.gov.hmcts.cmc.ccd.migration.config;

import org.skife.jdbi.v2.DBI;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.cmc.ccd.migration.repositories.ClaimRepository;

import javax.sql.DataSource;

@Configuration
public class EtlDbConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().build();
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
}
