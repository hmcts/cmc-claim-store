package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;

public final class JsonMapperFactory {

    private JsonMapperFactory() {
        // Utility class
    }

    public static JsonMapper create() {
        return new JsonMapper(new JacksonConfiguration().objectMapper());
    }
}
