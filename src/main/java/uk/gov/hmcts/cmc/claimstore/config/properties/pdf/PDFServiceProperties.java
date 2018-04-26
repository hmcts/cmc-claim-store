package uk.gov.hmcts.cmc.claimstore.config.properties.pdf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import javax.validation.constraints.NotNull;

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
