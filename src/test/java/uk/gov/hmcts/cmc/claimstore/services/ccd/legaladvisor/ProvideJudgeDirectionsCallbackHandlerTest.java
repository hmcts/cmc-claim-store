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
import uk.gov.hmcts.cmc.ccd.domain.CCDLaList;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
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
                .assignedTo(CCDLaList.REFER_TO_JUDGE)
                .build();
        when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

        when(caseDetailsConverter.convertToMap(ccdCase))
                .thenReturn(ImmutableMap.of("assignedTo", CCDLaList.FROM_JUDGE_WITH_DIRECTION));

        CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .eventId(CaseEvent.PROVIDE_DIRECTIONS.getValue())
                .build();

        CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .request(callbackRequest)
                .build();

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

        Assertions.assertSame(CCDLaList.FROM_JUDGE_WITH_DIRECTION, response.getData().get("assignedTo"));

        CCDCase updatedCcdCase = CCDCase.builder()
                .assignedTo(CCDLaList.FROM_JUDGE_WITH_DIRECTION)
                .build();
        verify(caseDetailsConverter).convertToMap(updatedCcdCase);
    }
}
