package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cmc.ccd_adapter.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDCaseHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDTestingSupportHandler;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.support.CCDTestingSupportRepository;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CCDCreateCaseService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

@Configuration
@ConditionalOnProperty(prefix = "feature_toggles", name = "ccd_async_enabled", havingValue = "true")
public class CCDStoreConfiguration {

    @Bean
    public ReferenceNumberService referenceNumberService(ReferenceNumberRepository referenceNumberRepository) {
        return new ReferenceNumberService(referenceNumberRepository);
    }

    @Bean
    public CCDCreateCaseService ccdCreateCaseService(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CaseAccessApi caseAccessApi,
        UserService userService
    ) {
        return new CCDCreateCaseService(
            coreCaseDataApi,
            authTokenGenerator,
            caseAccessApi,
            userService
        );
    }

    @Bean
    public CoreCaseDataService coreCaseDataService(
        CaseMapper caseMapper,
        UserService userService,
        ReferenceNumberService referenceNumberService,
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        JobSchedulerService jobSchedulerService,
        CCDCreateCaseService ccdCreateCaseService,
        CaseDetailsConverter caseDetailsConverter
    ) {
        return new CoreCaseDataService(caseMapper, userService, referenceNumberService, coreCaseDataApi,
            authTokenGenerator, jobSchedulerService, ccdCreateCaseService, caseDetailsConverter);
    }

    @Bean
    public CCDCaseApi ccdCaseApi(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        CaseAccessApi caseAccessApi,
        CoreCaseDataService coreCaseDataService,
        CaseDetailsConverter caseDetailsConverter,
        JobSchedulerService jobSchedulerService
    ) {
        return new CCDCaseApi(coreCaseDataApi, authTokenGenerator, userService, caseAccessApi,
            coreCaseDataService, caseDetailsConverter, jobSchedulerService, true);
    }

    @Bean
    public CCDTestingSupportRepository ccdSupportRepository(
        UserService userService,
        CCDCaseApi ccdCaseApi,
        CoreCaseDataService coreCaseDataService
    ) {
        return new CCDTestingSupportRepository(userService, ccdCaseApi, coreCaseDataService);
    }

    @Bean
    public CCDCaseRepository ccdCaseRepository(
        CCDCaseApi ccdCaseApi,
        CoreCaseDataService coreCaseDataService,
        UserService userService
    ) {
        return new CCDCaseRepository(ccdCaseApi, coreCaseDataService, userService);
    }

    @Bean
    public CCDCaseHandler ccdCaseHandler(
        CCDCaseRepository ccdCaseRepository,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator,
        AppInsights appInsights,
        UserService userService
    ) {
        return new CCDCaseHandler(ccdCaseRepository, directionsQuestionnaireDeadlineCalculator,
            appInsights, userService);
    }

    @Bean
    @ConditionalOnProperty("claim-store.test-support.enabled")
    public CCDTestingSupportHandler ccdTestingSupportHandler(
        CCDTestingSupportRepository ccdSupportRepository
    ) {
        return new CCDTestingSupportHandler(ccdSupportRepository);
    }
}
