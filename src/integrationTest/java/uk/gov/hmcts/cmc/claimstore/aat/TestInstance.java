package uk.gov.hmcts.cmc.claimstore.aat;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("aat-tests")
@ConfigurationProperties(prefix = "aat.test-instance")
public class TestInstance {

    @NotBlank
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
