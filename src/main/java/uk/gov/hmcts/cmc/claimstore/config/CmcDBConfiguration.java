package uk.gov.hmcts.cmc.claimstore.config;

import org.flywaydb.core.Flyway;
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
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.UserRolesRepository;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties
public class CmcDBConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSourceProxy(
        DataSource dataSource
    ) {
        TransactionAwareDataSourceProxy dataSourceProxy = new TransactionAwareDataSourceProxy(dataSource);

        migrateFlyway(dataSourceProxy);
        return dataSourceProxy;
    }

    private void migrateFlyway(DataSource dataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("db/migration")
            .load()
            .migrate();
    }

    @Bean
    public PlatformTransactionManager transactionManager(
        TransactionAwareDataSourceProxy transactionAwareDataSourceProxy
    ) {
        return new DataSourceTransactionManager(transactionAwareDataSourceProxy);
    }

    @Bean
    public DBI dbi(
        TransactionAwareDataSourceProxy transactionAwareDataSourceProxy
    ) {
        DBI dbi = new DBI(transactionAwareDataSourceProxy);
        dbi.registerContainerFactory(new OptionalContainerFactory());

        return dbi;
    }

    @Bean
    public UserRolesRepository userRolesRepository(DBI dbi) {
        return dbi.onDemand(UserRolesRepository.class);
    }

    @Bean
    public ReferenceNumberRepository referenceNumberRepository(DBI dbi) {
        return dbi.onDemand(ReferenceNumberRepository.class);
    }
}
