package uk.gov.hmcts.cmc.claimstore.events.ccd;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
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
    AppInsights appInsights;
    @Mock
    UserService userService;
    private static final String AUTHORISATION = "Bearer: aaa";

    private CCDCaseHandler caseHandler;

    @Before
    public void setup() {
        caseHandler = new CCDCaseHandler(ccdCaseRepository, directionsQuestionnaireDeadlineCalculator,
            appInsights, userService);
    }

    @Test
    public void saveClaimShouldCallCCDCaseRepository() {
        Claim sampleClaim = SampleClaim.getDefault();

        CCDClaimIssuedEvent claimIssuedEvent = new CCDClaimIssuedEvent(sampleClaim, AUTHORISATION);
        caseHandler.saveClaim(claimIssuedEvent);

        verify(ccdCaseRepository, times(1))
            .saveClaim(claimIssuedEvent.getAuthorization(), claimIssuedEvent.getClaim());
    }

    @Test(expected = FeignException.class)
    public void saveClaimFailsWithFeignExceptionShouldTriggerAppInsight() {
        Claim sampleClaim = SampleClaim.getDefault();

        when(ccdCaseRepository.saveClaim(AUTHORISATION, sampleClaim))
            .thenThrow(FeignException.class);

        CCDClaimIssuedEvent claimIssuedEvent = new CCDClaimIssuedEvent(sampleClaim, AUTHORISATION);
        caseHandler.saveClaim(claimIssuedEvent);

        verifyZeroInteractions(ccdCaseRepository);
        verify(appInsights, times(1))
            .trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, sampleClaim.getReferenceNumber());

    }
}
