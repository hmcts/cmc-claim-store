package uk.gov.hmcts.cmc.claimstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class,
    scanBasePackages = {"uk.gov.hmcts.cmc.claimstore", "uk.gov.hmcts.document"})
@SuppressWarnings({"HideUtilityClassConstructor", "squid:S1118"}) // Spring needs a constructor, its not a utility class
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"uk.gov.hmcts.cmc.claimstore", "uk.gov.hmcts.document"})
public class ClaimStoreApplication {

    public static final String BASE_PACKAGE_NAME = ClaimStoreApplication.class.getPackage().getName();

    public static void main(String[] args) {
        SpringApplication.run(ClaimStoreApplication.class, args);
    }
}

