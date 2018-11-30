package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.ccd.mapper.claimantresponse.ClaimantResponseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.offers.SettlementMapper;
import uk.gov.hmcts.cmc.ccd.mapper.response.ResponseMapper;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDCaseHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccd.CCDTestingSupportHandler;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.support.CCDTestingSupportRepository;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.utils.CCDCaseDataToClaim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.util.concurrent.Executor;

@Configuration
@EnableRetry
@EnableAsync
@ConditionalOnProperty(prefix = "feature_toggles", name = "ccd_async_enabled")
public class CCDStoreConfiguration {

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor(@Value("${async.max.threadPool.size}") Integer maxThreadPoolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxThreadPoolSize);
        executor.initialize();
        return executor;
    }

    @Bean
    public ReferenceNumberService referenceNumberService(ReferenceNumberRepository referenceNumberRepository) {
        return new ReferenceNumberService(referenceNumberRepository);
    }

    @Bean
    public CoreCaseDataService coreCaseDataService(
        CaseMapper caseMapper,
        CountyCourtJudgmentMapper countyCourtJudgmentMapper,
        ResponseMapper responseMapper,
        SettlementMapper settlementMapper,
        ClaimantResponseMapper claimantResponseMapper,
        UserService userService,
        JsonMapper jsonMapper,
        ReferenceNumberService referenceNumberService,
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CaseAccessApi caseAccessApi,
        JobSchedulerService jobSchedulerService
    ) {
        return new CoreCaseDataService(caseMapper, countyCourtJudgmentMapper, responseMapper, settlementMapper,
            claimantResponseMapper, userService, jsonMapper, referenceNumberService, coreCaseDataApi,
            authTokenGenerator, caseAccessApi, jobSchedulerService);
    }

    @Bean
    public CCDCaseApi ccdCaseApi(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        CaseAccessApi caseAccessApi,
        CoreCaseDataService coreCaseDataService,
        CCDCaseDataToClaim ccdCaseDataToClaim,
        JobSchedulerService jobSchedulerService
    ) {
        return new CCDCaseApi(coreCaseDataApi, authTokenGenerator, userService, caseAccessApi,
            coreCaseDataService, ccdCaseDataToClaim, jobSchedulerService, true);
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
        CoreCaseDataService coreCaseDataService
    ) {
        return new CCDCaseRepository(ccdCaseApi, coreCaseDataService);
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
