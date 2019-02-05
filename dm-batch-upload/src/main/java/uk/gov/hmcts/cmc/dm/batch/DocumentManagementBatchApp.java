package uk.gov.hmcts.cmc.dm.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SuppressWarnings({"HideUtilityClassConstructor", "squid:S1118"}) // Spring needs a constructor, its not a utility class
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class},
    scanBasePackages = {
        "uk.gov.hmcts.cmc.dm.batch"
    })
@EnableBatchProcessing
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.cmc.ccd.migration.idam.api"
    }
)
public class DocumentManagementBatchApp {

    private static final Logger log = LoggerFactory.getLogger(DocumentManagementBatchApp.class);

    public static void main(String[] args) {
        log.info("Document Management Batch App started");
        SpringApplication.run(DocumentManagementBatchApp.class);
    }

}
