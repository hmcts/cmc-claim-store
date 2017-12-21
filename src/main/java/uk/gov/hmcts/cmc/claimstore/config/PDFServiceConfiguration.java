package uk.gov.hmcts.cmc.claimstore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.PDFServiceProperties;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

@Configuration
public class PDFServiceConfiguration {

    @Bean
    public PDFServiceClient pdfServiceClient(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        AuthTokenGenerator authTokenGenerator,
        PDFServiceProperties properties
    ) {
        return PDFServiceClient.builder()
            .restOperations(restTemplate)
            .objectMapper(objectMapper)
            .build(authTokenGenerator::generate, properties.getUrl());
    }

}
