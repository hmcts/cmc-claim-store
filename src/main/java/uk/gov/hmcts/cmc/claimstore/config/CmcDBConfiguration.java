package uk.gov.hmcts.cmc.claimstore.config;

import org.flywaydb.core.Flyway;
import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @ConfigurationProperties("spring.datasource.cmc")
    public DataSourceProperties cmcDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean("cmcDataSource")
    @ConfigurationProperties("spring.datasource.cmc")
    public DataSource cmcDataSource() {
        return cmcDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("cmcTransactionAwareDataSourceProxy")
    public TransactionAwareDataSourceProxy transactionAwareDataSourceProxy(
        @Qualifier("cmcDataSource") DataSource dataSource
    ) {
        TransactionAwareDataSourceProxy dataSourceProxy = new TransactionAwareDataSourceProxy(dataSource);

        migrateFlyway(dataSourceProxy);
        return dataSourceProxy;
    }

    private void migrateFlyway(DataSource dataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("cmc/db/migration")
            .load()
            .migrate();
    }

    @Bean("cmcTransactionManager")
    public PlatformTransactionManager transactionManager(
        @Qualifier("cmcTransactionAwareDataSourceProxy")
            TransactionAwareDataSourceProxy cmcTransactionAwareDataSourceProxy
    ) {
        return new DataSourceTransactionManager(cmcTransactionAwareDataSourceProxy);
    }

    @Bean("cmcDbi")
    public DBI dbi(@Qualifier("cmcTransactionAwareDataSourceProxy")
                       TransactionAwareDataSourceProxy cmcTransactionAwareDataSourceProxy
    ) {
        DBI dbi = new DBI(cmcTransactionAwareDataSourceProxy);
        dbi.registerContainerFactory(new OptionalContainerFactory());

        return dbi;
    }

    @Bean
    public UserRolesRepository userRolesRepository(@Qualifier("cmcDbi") DBI dbi) {
        return dbi.onDemand(UserRolesRepository.class);
    }

    @Bean
    public ReferenceNumberRepository referenceNumberRepository(@Qualifier("cmcDbi") DBI dbi) {
        return dbi.onDemand(ReferenceNumberRepository.class);
    }
}
