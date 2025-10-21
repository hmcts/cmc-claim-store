package uk.gov.hmcts.cmc.claimstore.config.properties.pdf;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Component
@Validated
@ConfigurationProperties(prefix = "pdf-service")
public class PDFServiceProperties {

    @NotNull
    private URI url;

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

}
