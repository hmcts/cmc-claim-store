package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectionsQuestionnaireDeadlineCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private DirectionsQuestionnaireDeadlineCallbackHandler handler;
    private CallbackParams params;
    private CCDCase ccdCase;

    @BeforeEach
    void setUp() {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(ImmutableMap.of()).build())
            .build();

        params = CallbackParams.builder()
           .type(CallbackType.ABOUT_TO_SUBMIT)
           .request(request)
           .build();

        ccdCase = CCDCase.builder()
            .build();
        when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

    }

    @Test
    void shouldReturnOpenStateIfCtscNotEnabled() {
        handler = new DirectionsQuestionnaireDeadlineCallbackHandler(caseDetailsConverter, false);

        String state = ClaimState.OPEN.getValue();
        when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CCDCase updatedCcdCase = CCDCase.builder()
            .state(state)
            .build();
        verify(caseDetailsConverter).convertToMap(updatedCcdCase);

        Assertions.assertEquals(state, response.getData().get("state"));
    }

    @Test
    void shouldReturnReadyForPaperDQStateIfCtscEnabled() {
        handler = new DirectionsQuestionnaireDeadlineCallbackHandler(caseDetailsConverter, true);

        String state = ClaimState.READY_FOR_PAPER_DQ.getValue();
        when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CCDCase updatedCcdCase = CCDCase.builder()
            .state(state)
            .build();
        verify(caseDetailsConverter).convertToMap(updatedCcdCase);

        Assertions.assertEquals(state, response.getData().get("state"));

    }
}
