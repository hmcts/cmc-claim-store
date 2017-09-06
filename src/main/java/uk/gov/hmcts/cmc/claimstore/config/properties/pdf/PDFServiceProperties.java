package uk.gov.hmcts.cmc.claimstore.config.properties.pdf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import javax.validation.constraints.NotNull;

@Component
@Validated
@ConfigurationProperties(prefix = "pdfService")
public class PDFServiceProperties {

    @NotNull
    private URI baseUrl;

    public URI getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

}
