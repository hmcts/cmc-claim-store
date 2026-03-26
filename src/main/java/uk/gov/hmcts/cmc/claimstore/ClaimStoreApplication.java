package uk.gov.hmcts.cmc.claimstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        DataSourcePoolMetricsAutoConfiguration.class
    },
    scanBasePackages = {
        "uk.gov.hmcts.cmc",
        "uk.gov.hmcts.reform.fees.client"
    }
)
@SuppressWarnings({"HideUtilityClassConstructor", "squid:S1118"}) // Spring needs a constructor, its not a utility class
@EnableFeignClients(basePackages =
    {"uk.gov.hmcts.cmc.claimstore",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.docassembly",
        "uk.gov.hmcts.reform.document",
        "uk.gov.hmcts.reform.fees.client",
        "uk.gov.hmcts.reform.payments",
        "uk.gov.hmcts.reform.sendletter",
        "uk.gov.hmcts.reform.ccd.client",
        "uk.gov.hmcts.reform.ccd.document.am"
    })
public class ClaimStoreApplication {

    public static final String BASE_PACKAGE_NAME = ClaimStoreApplication.class.getPackage().getName();

    public static void main(String[] args) {
        SpringApplication.run(ClaimStoreApplication.class, args);
    }
}
