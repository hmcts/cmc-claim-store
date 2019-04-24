package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;

@Configuration
@EnableRetry
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentManagementConfiguration {

    @Bean
    public DocumentManagementService documentManagementService(
        DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        DocumentDownloadClientApi documentDownloadClientApi,
        DocumentUploadClientApi documentUploadClientApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        AppInsights appInsights
    ) {
        return new DocumentManagementService(
            documentMetadataDownloadApi,
            documentDownloadClientApi,
            documentUploadClientApi,
            authTokenGenerator,
            userService,
            appInsights
        );
    }
}
