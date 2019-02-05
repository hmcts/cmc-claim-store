package uk.gov.hmcts.cmc.dm.batch.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cmc.dm.batch.idam.models.User;
import uk.gov.hmcts.cmc.dm.batch.idam.services.UserService;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Autowired
    private UserService userService;

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

    @Bean
    public User user() {
        return userService.authenticateSystemUpdateUser();
    }

}
