package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.LegalOrderGenerationDeadlinesCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.GenerateOrderCallbackService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;

@Configuration
@EnableRetry
public class DocAssemblyConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "doc_assembly", name = "url")
    public GenerateOrderCallbackService generateOrderCallbackServiceWithClient(
        UserService userService,
        LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator,
        DocAssemblyClient docAssemblyClient,
        AuthTokenGenerator authTokenGenerator,
        JsonMapper jsonMapper,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper
    ) {
        return new GenerateOrderCallbackService(
            userService,
            legalOrderGenerationDeadlinesCalculator,
            docAssemblyClient,
            authTokenGenerator,
            jsonMapper,
            docAssemblyTemplateBodyMapper
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public GenerateOrderCallbackService generateOrderCallbackServiceWithoutClient(
        UserService userService,
        LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator,
        AuthTokenGenerator authTokenGenerator,
        JsonMapper jsonMapper,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper
    ) {
        return new GenerateOrderCallbackService(
            userService,
            legalOrderGenerationDeadlinesCalculator,
            null,
            authTokenGenerator,
            jsonMapper,
            docAssemblyTemplateBodyMapper
        );
    }
}
