package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.docassembly.DocAssemblyApi;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;

@Configuration
public class DocAssemblyClientConfiguration {

    @Bean
    public DocAssemblyClient docAssemblyClient(DocAssemblyApi docAssemblyApi) {
        return new DocAssemblyClient(docAssemblyApi);
    }
}
