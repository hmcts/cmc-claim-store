package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDLaList;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REVIEW_JUDGE_COMMENTS;

@ExtendWith(MockitoExtension.class)
class ReviewJudgeCommentsCallbackHandlerTest {

    @Mock
    CaseDetailsConverter caseDetailsConverter;

    @Captor
    private ArgumentCaptor<CCDCase> ccdCaseArgumentCaptor;

    private ReviewJudgeCommentsCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReviewJudgeCommentsCallbackHandler(caseDetailsConverter);
    }

    @Test
    void shouldSetAssignedToNull() {
        CCDCase ccdCase = CCDCase.builder()
            .assignedTo(CCDLaList.REFER_TO_JUDGE)
            .build();
        when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

        when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of());

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(REVIEW_JUDGE_COMMENTS.getValue())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

        Assertions.assertNull(response.getData().get("assignedTo"));

        verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());
        CCDCase updatedCcdCase = CCDCase.builder()
            .assignedTo(null)
            .build();
        Assertions.assertEquals(updatedCcdCase, ccdCaseArgumentCaptor.getValue());

    }
}
