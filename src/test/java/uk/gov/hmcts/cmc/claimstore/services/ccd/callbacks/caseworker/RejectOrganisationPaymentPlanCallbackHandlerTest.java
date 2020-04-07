package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REJECT_ORGANISATION_PAYMENT_PLAN;

@ExtendWith(MockitoExtension.class)
class RejectOrganisationPaymentPlanCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Captor
    private ArgumentCaptor<CCDCase> ccdCaseArgumentCaptor;

    private RejectOrganisationPaymentPlanCallbackHandler handler;

    private CallbackParams params;

    private CCDCase ccdCase;

    @BeforeEach
    void setUp() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(REJECT_ORGANISATION_PAYMENT_PLAN.getValue())
            .build();

        params = CallbackParams.builder()
            .request(callbackRequest)
            .type(CallbackType.ABOUT_TO_SUBMIT).build();

        ccdCase = CCDCase.builder()
            .build();
        when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);
    }

    @Test
    void shouldReturnOpenStateIfCtscNotEnabled() {

        handler = new RejectOrganisationPaymentPlanCallbackHandler(caseDetailsConverter, false);

        String state = ClaimState.OPEN.getValue();
        when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());
        CCDCase updatedCcdCase = CCDCase.builder()
            .state(state)
            .build();
        Assertions.assertEquals(updatedCcdCase, ccdCaseArgumentCaptor.getValue());
        Assertions.assertEquals(state, response.getData().get("state"));
    }

    @Test
    void shouldReturnJudgmentDecideAmountIfCtscEnabled() {

        handler = new RejectOrganisationPaymentPlanCallbackHandler(caseDetailsConverter, true);

        String state = ClaimState.JUDGMENT_DECIDE_AMOUNT.getValue();
        when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());
        CCDCase updatedCcdCase = CCDCase.builder()
            .state(state)
            .build();
        Assertions.assertEquals(updatedCcdCase, ccdCaseArgumentCaptor.getValue());
        Assertions.assertEquals(state, response.getData().get("state"));
    }
}
