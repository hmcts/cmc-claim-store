package uk.gov.hmcts.cmc.claimstore.config;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.docassembly.DocAssemblyApi;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DocAssemblyClientConfigurationTest {

    private final DocAssemblyClientConfiguration configuration = new DocAssemblyClientConfiguration();

    @Test
    void shouldCreateDocAssemblyClientBean() {
        DocAssemblyApi docAssemblyApi = mock(DocAssemblyApi.class);

        DocAssemblyClient client = configuration.docAssemblyClient(docAssemblyApi);

        assertThat(client).isNotNull();
    }
}
