package uk.gov.hmcts.cmc.claimstore.api.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworkerProperties;
import uk.gov.hmcts.cmc.claimstore.controllers.ClaimController;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.models.idam.Oauth2;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimAuthorisationRule;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.rules.PaidInFullRule;
import uk.gov.hmcts.cmc.claimstore.rules.ReviewOrderRule;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.services.user.UserAuthorisationTokenService;
import uk.gov.hmcts.cmc.claimstore.services.user.UserInfoService;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

@Configuration
public class GetClaimsContractConfig {

    @MockitoBean
    private IssueDateCalculator issueDateCalculator;
    @MockitoBean
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @MockitoBean
    private EventProducer eventProducer;
    @MockitoBean
    private MoreTimeRequestRule moreTimeRequestRule;
    @MockitoBean
    private AppInsights appInsights;
    @MockitoBean
    private PaidInFullRule paidInFullRule;
    @MockitoBean
    private ReviewOrderRule reviewOrderRule;
    @MockitoBean
    private LaunchDarklyClient launchDarklyClient;
    @MockitoBean
    private IdamApi idamApi;
    @MockitoBean
    private IdamCaseworkerProperties idamCaseworkerProperties;
    @MockitoBean
    private Oauth2 oauth2;
    @MockitoBean
    private CCDCaseApi ccdCaseApi;
    @MockitoBean
    private CoreCaseDataService coreCaseDataService;

    @Bean
    @Primary
    public ClaimController claimController() {
        return new ClaimController(claimService());
    }

    @Bean
    public ClaimAuthorisationRule claimAuthorisationRule() {
        return new ClaimAuthorisationRule(userService());
    }

    @Bean
    public UserInfoService userInfoService() {
        return new UserInfoService(idamApi);
    }

    @Bean
    public UserAuthorisationTokenService userAuthorisationTokenService() {
        return new UserAuthorisationTokenService(idamApi, oauth2);
    }

    @Bean
    public UserService userService() {
        return new UserService(idamApi, idamCaseworkerProperties, oauth2, userInfoService(), userAuthorisationTokenService());
    }

    @Bean
    public CCDCaseRepository ccdCaseRepository() {
        return new CCDCaseRepository(ccdCaseApi, coreCaseDataService, userService());
    }

    @Bean
    public ClaimService claimService() {
        return new ClaimService(
            ccdCaseRepository(),
            userService(),
            issueDateCalculator,
            responseDeadlineCalculator,
            moreTimeRequestRule,
            eventProducer,
            appInsights,
            paidInFullRule,
            claimAuthorisationRule(),
            reviewOrderRule,
            launchDarklyClient,
            true);
    }
}
