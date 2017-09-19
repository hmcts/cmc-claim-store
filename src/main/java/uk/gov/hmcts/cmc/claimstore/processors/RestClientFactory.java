package uk.gov.hmcts.cmc.claimstore.processors;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.clients.RestClient;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;

@Component
public class RestClientFactory {

    public RestClient create(final String url) {
        return RestClient.Builder.of().apiDomain(url)
            .jsonMapper(JsonMapperFactory.create())
            .build();
    }
}
