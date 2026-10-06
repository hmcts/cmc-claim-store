package uk.gov.hmcts.cmc.claimstore.api.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

import static org.mockito.Mockito.mock;

@Configuration
public class GetClaimsContractConfig {

    @Bean
    public IssueDateCalculator issueDateCalculator() {
        return mock(IssueDateCalculator.class);
    }

    @Bean
    public ResponseDeadlineCalculator responseDeadlineCalculator() {
        return mock(ResponseDeadlineCalculator.class);
    }

    @Bean
    public EventProducer eventProducer() {
        return mock(EventProducer.class);
    }

    @Bean
    public MoreTimeRequestRule moreTimeRequestRule() {
        return mock(MoreTimeRequestRule.class);
    }

    @Bean
    public AppInsights appInsights() {
        return mock(AppInsights.class);
    }

    @Bean
    public PaidInFullRule paidInFullRule() {
        return mock(PaidInFullRule.class);
    }

    @Bean
    public ReviewOrderRule reviewOrderRule() {
        return mock(ReviewOrderRule.class);
    }

    @Bean
    public LaunchDarklyClient launchDarklyClient() {
        return mock(LaunchDarklyClient.class);
    }

    @Bean
    public IdamApi idamApi() {
        return mock(IdamApi.class);
    }

    @Bean
    public IdamCaseworkerProperties idamCaseworkerProperties() {
        return mock(IdamCaseworkerProperties.class);
    }

    @Bean
    public Oauth2 oauth2() {
        return mock(Oauth2.class);
    }

    @Bean
    public CCDCaseApi ccdCaseApi() {
        return mock(CCDCaseApi.class);
    }

    @Bean
    public CoreCaseDataService coreCaseDataService() {
        return mock(CoreCaseDataService.class);
    }

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
        return new UserInfoService(idamApi());
    }

    @Bean
    public UserAuthorisationTokenService userAuthorisationTokenService() {
        return new UserAuthorisationTokenService(idamApi(), oauth2());
    }

    @Bean
    public UserService userService() {
        return new UserService(idamApi(), idamCaseworkerProperties(), oauth2(), userInfoService(), userAuthorisationTokenService());
    }

    @Bean
    public CCDCaseRepository ccdCaseRepository() {
        return new CCDCaseRepository(ccdCaseApi(), coreCaseDataService(), userService());
    }

    @Bean
    public ClaimService claimService() {
        return new ClaimService(
            ccdCaseRepository(),
            userService(),
            issueDateCalculator(),
            responseDeadlineCalculator(),
            moreTimeRequestRule(),
            eventProducer(),
            appInsights(),
            paidInFullRule(),
            claimAuthorisationRule(),
            reviewOrderRule(),
            launchDarklyClient(),
            true);
    }
}
