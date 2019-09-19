package uk.gov.hmcts.cmc.claimstore.events.ccd;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCD_ASYNC_FAILURE;

@RunWith(MockitoJUnitRunner.class)
public class CCDCaseHandlerTest {

    @Mock
    private CCDCaseRepository ccdCaseRepository;
    @Mock
    private DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;
    @Mock
    private AppInsights appInsights;
    @Mock
    private UserService userService;

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder().build();

    private CCDCaseHandler caseHandler;
    private User user;

    @Before
    public void setUp() {
        caseHandler = new CCDCaseHandler(ccdCaseRepository, directionsQuestionnaireDeadlineCalculator,
            appInsights, userService);
        user = new User(AUTHORISATION, USER_DETAILS);
    }

    @Test
    public void saveClaimShouldCallCCDCaseRepository() {
        Claim sampleClaim = SampleClaim.getDefault();

        CCDClaimIssuedEvent claimIssuedEvent = new CCDClaimIssuedEvent(sampleClaim, user);
        caseHandler.saveClaim(claimIssuedEvent);

        verify(ccdCaseRepository, times(1))
            .saveClaim(claimIssuedEvent.getUser(), claimIssuedEvent.getClaim());
    }

    @Test(expected = FeignException.class)
    public void saveClaimFailsWithFeignExceptionShouldTriggerAppInsight() {
        Claim sampleClaim = SampleClaim.getDefault();

        when(ccdCaseRepository.saveClaim(user, sampleClaim))
            .thenThrow(FeignException.class);

        CCDClaimIssuedEvent claimIssuedEvent = new CCDClaimIssuedEvent(sampleClaim, user);
        caseHandler.saveClaim(claimIssuedEvent);

        verifyZeroInteractions(ccdCaseRepository);
        verify(appInsights, times(1))
            .trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, sampleClaim.getReferenceNumber());

    }
}
