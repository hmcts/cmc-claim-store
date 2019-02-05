package uk.gov.hmcts.cmc.dm.batch.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DBConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.claimstore")
    public DataSourceProperties claimStoreDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.claimstore")
    public DataSource dataSource() {
        return claimStoreDataSourceProperties().initializeDataSourceBuilder().build();
    }

}
