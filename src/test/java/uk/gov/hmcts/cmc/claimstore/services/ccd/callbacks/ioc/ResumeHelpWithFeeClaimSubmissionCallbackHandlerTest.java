package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.UPDATE_HELP_WITH_FEE_CLAIM;

@ExtendWith(MockitoExtension.class)
 class ResumeHelpWithFeeClaimSubmissionCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseDetails caseDetails;
    private CallbackRequest callbackRequest;
    private CallbackParams callbackParams;
    private static final String BEARER_TOKEN = "Bearer a";
    private ResumeHelpWithFeeClaimSubmissionCallbackHandler resumeHelpWithFeeClaimSubmissionCallbackHandler;

    @BeforeEach
    void setUp() {
        resumeHelpWithFeeClaimSubmissionCallbackHandler = new
            ResumeHelpWithFeeClaimSubmissionCallbackHandler(caseDetailsConverter);
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(UPDATE_HELP_WITH_FEE_CLAIM.getValue())
            .build();
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }

    @Test
    void shouldResumeHelpWithFeeClaimSubmissionCallbackReturnError() {
        CCDCase ccdCase = SampleData.getCCDCitizenCaseAwaitingayment();
        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            resumeHelpWithFeeClaimSubmissionCallbackHandler.aboutToSubmit(callbackParams);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldResumeHelpWithFeeClaimSubmissionCallbackReturnPayment() {
        CCDCase ccdCase = SampleData.getCCDCitizenCaseAwaitingayment();
        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            resumeHelpWithFeeClaimSubmissionCallbackHandler.aboutToSubmit(callbackParams);
        Assertions.assertNull(response.getData().get("paymentReference"));
        Assertions.assertNull(response.getData().get("paymentStatus"));
        Assertions.assertNull(response.getData().get("paymentTransactionId"));
        Assertions.assertNull(response.getData().get("paymentAmount"));
        Assertions.assertNull(response.getData().get("paymentDateCreated"));
        Assertions.assertNull(response.getData().get("paymentNextUrl"));
    }
}
