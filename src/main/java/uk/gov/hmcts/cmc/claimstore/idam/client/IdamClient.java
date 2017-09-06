package uk.gov.hmcts.cmc.claimstore.idam.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.clients.RestClient;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinRequest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.RestClientFactory;

@Component
public class IdamClient {

    public static final class Endpoints {

        public static final String PIN = "/pin";
        public static final String DETAILS = "/details";

        private Endpoints() {
            // Utility class
        }

    }

    private final RestClient client;

    public IdamClient(final RestClientFactory clientFactory, @Value("${idam.api.url}") final String url) {
        this.client = clientFactory.create(url);
    }

    public GeneratePinResponse generatePin(final GeneratePinRequest requestBody, final String authorisationToken) {
        ResponseEntity<GeneratePinResponse> value = client.post(
            Endpoints.PIN, requestBody, authorisationToken, GeneratePinResponse.class
        );

        return value.getBody();
    }

    public UserDetails retrieveUserDetails(String authorisationHeader) {
        return client.get(Endpoints.DETAILS, authorisationHeader, UserDetails.class);
    }

}
