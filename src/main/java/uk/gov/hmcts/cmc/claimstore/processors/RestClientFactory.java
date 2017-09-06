package uk.gov.hmcts.cmc.claimstore.processors;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.clients.RestClient;

@Component
public class RestClientFactory {
    private JsonMapper jsonMapper;

    public RestClientFactory(final JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public RestClient create(final String url) {
        return RestClient.Builder.of().apiDomain(url).jsonMapper(jsonMapper).build();
    }
}
