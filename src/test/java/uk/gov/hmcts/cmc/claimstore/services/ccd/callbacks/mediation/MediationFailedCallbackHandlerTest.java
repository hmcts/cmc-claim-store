package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHearingLocation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MEDIATION_FAILED;

@RunWith(MockitoJUnitRunner.class)
public class MediationFailedCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private MediationFailedCallbackHandler mediationFailedCallbackHandler;

    private CallbackParams callbackParams;
    private static final String AUTHORISATION = "Bearer: aaaa";

    private Claim claimSetForMediation =
        SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();

    @Before
    public void setUp() {
        mediationFailedCallbackHandler = new MediationFailedCallbackHandler(caseDetailsConverter);
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(MEDIATION_FAILED.getValue())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void throwsExceptionIfNotDefenseOrFullAdmit() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        Claim claim = SampleClaim.getClaimWithFullAdmission();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        mediationFailedCallbackHandler.handle(callbackParams);
    }

    @Test(expected = IllegalStateException.class)
    public void throwsExceptionIfClaimantResponseAcceptation() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        Claim claim = SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        mediationFailedCallbackHandler.handle(callbackParams);
    }

    @Test
    public void setsToOpenIfNotOnlineDQCase() {

        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        Claim claim = claimSetForMediation.toBuilder()
            .claimData(SampleClaimData.submittedWithAmountMoreThanThousand())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            mediationFailedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).containsEntry("state", "open");

    }

    @Test
    public void setsStateToReadyForTransferIfNotPilotCase() {

        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        Claim claim = claimSetForMediation.toBuilder()
            .features(Collections.singletonList("directionsQuestionnaire"))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            mediationFailedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).containsEntry("state", "readyForTransfer");

    }

    @Test
    public void setsToReadyForDirectionsIfPilotCase() {

        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        Claim claim = claimSetForMediation.toBuilder()
            .response(
                SampleResponse
                    .FullDefence
                    .builder()
                    .withDirectionsQuestionnaire(
                        SampleDirectionsQuestionnaire.builder()
                            .withHearingLocation(SampleHearingLocation.pilotHearingLocation)
                            .build()
                    ).build()
            )
            .features(Collections.singletonList("directionsQuestionnaire"))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            mediationFailedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).containsEntry("state", "readyForDirections");
    }
}
