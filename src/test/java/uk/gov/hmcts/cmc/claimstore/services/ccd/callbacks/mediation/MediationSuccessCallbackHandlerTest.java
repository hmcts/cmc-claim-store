package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils.DQ_FLAG;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class MediationSuccessCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private AppInsights appInsights;

    private MediationSuccessCallbackHandler mediationSuccessCallbackHandler;

    private static final String AUTHORISATION = "Bearer: aaaa";

    private Claim claimSetForMediation =
        SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();

    private CallbackParams callbackParams;

    @Before
    public void setUp() {
        mediationSuccessCallbackHandler = new MediationSuccessCallbackHandler(caseDetailsConverter, appInsights);

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.MEDIATION_SUCCESSFUL.getValue())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    @Test
    public void shouldRaiseAppInsightWhenFeatureIsMediationPilot() {

        Claim claim = claimSetForMediation.toBuilder()
            .features(Collections.singletonList(FeaturesUtils.MEDIATION_PILOT))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        mediationSuccessCallbackHandler.handle(callbackParams);

        verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.MEDIATION_PILOT_SUCCESS),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void shouldRaiseAppInsightWhenFeatureIsNotMediationPilot() {

        Claim claim = claimSetForMediation.toBuilder()
            .features(Collections.singletonList(DQ_FLAG))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        mediationSuccessCallbackHandler.handle(callbackParams);

        verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.NON_MEDIATION_PILOT_SUCCESS),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }
}
