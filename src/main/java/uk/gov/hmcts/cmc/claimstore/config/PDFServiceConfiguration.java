package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.PDFServiceProperties;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

@Configuration
public class PDFServiceConfiguration {

    @Bean
    public PDFServiceClient pdfServiceClient(PDFServiceProperties properties) {
        return new PDFServiceClient(properties.getBaseUrl(), "v1");
    }
}
