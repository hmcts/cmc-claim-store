package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDLaList;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReferToJudgeCallbackHandler1Test {

    private ReferToJudgeCallbackHandler1 handler;
    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    private static final String AUTHORISATION = "Bearer: aaaa";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @BeforeEach
    public void setUp() {

        handler = new ReferToJudgeCallbackHandler1(caseDetailsConverter);
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.EMPTY_MAP).build())
            .eventId(CaseEvent.REFER_TO_JUDGE.getValue())
            .build();
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
            .thenReturn(CCDCase.builder().build());
        when(caseDetailsConverter.convertToMap(any(CCDCase.class)))
            .thenReturn(ImmutableMap.of("assignedTo", CCDLaList.REFER_TO_JUDGE));
    }

    @Test
    void shouldAssignToOcmcBoxwork() {
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        Assertions.assertEquals(CCDLaList.REFER_TO_JUDGE, response.getData().get("assignedTo"));
    }
}
