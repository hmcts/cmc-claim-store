package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

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
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDLAList;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor.ProvideJudgeDirectionsCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProvideJudgeDirectionsCallbackHandlerTest {

    @Mock
    CaseDetailsConverter caseDetailsConverter;

    @Captor
    private ArgumentCaptor<CCDCase> ccdCaseArgumentCaptor;

    private ProvideJudgeDirectionsCallbackHandler handler;

    @BeforeEach
    void setUp() {
    handler = new ProvideJudgeDirectionsCallbackHandler(caseDetailsConverter);
    }

    @Test
    void shouldSetAssignedToFromJudgeWithDirections() {
    CCDCase ccdCase = CCDCase.builder()
            .assignedTo(CCDLAList.REFER_TO_JUDGE)
            .build();
    when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

    when(caseDetailsConverter.convertToMap(ccdCase))
            .thenReturn(ImmutableMap.of("assignedTo", CCDLAList.FROM_JUDGE_WITH_DIRECTION));

    CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CaseEvent.PROVIDE_DIRECTIONS.getValue())
            .build();

    CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();

    AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

    Assertions.assertSame(CCDLAList.FROM_JUDGE_WITH_DIRECTION, response.getData().get("assignedTo"));

    verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());
    CCDCase updatedCcdCase = CCDCase.builder()
            .assignedTo(CCDLAList.FROM_JUDGE_WITH_DIRECTION)
            .build();
    Assertions.assertEquals(updatedCcdCase, ccdCaseArgumentCaptor.getValue());

    }
}
