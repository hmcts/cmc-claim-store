package uk.gov.hmcts.cmc.claimstore.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties
public class CmcDBConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource.cmc")
    public DataSourceProperties cmcDataSourceProperties() {
        return new DataSourceProperties();
    }

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
        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("cmc/db/migration");
        flyway.migrate();
    }

    @Bean("cmcTransactionManager")
    public PlatformTransactionManager transactionManager(
        @Qualifier("cmcTransactionAwareDataSourceProxy")
            TransactionAwareDataSourceProxy cmcTransactionAwareDataSourceProxy
    ) {
        return new DataSourceTransactionManager(cmcTransactionAwareDataSourceProxy);
    }
}
