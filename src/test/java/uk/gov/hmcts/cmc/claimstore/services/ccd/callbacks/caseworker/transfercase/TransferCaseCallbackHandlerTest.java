package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.MID;

@ExtendWith(MockitoExtension.class)
public class TransferCaseCallbackHandlerTest {

    @InjectMocks
    private TransferCaseCallbackHandler handler;

    @Mock
    private TransferCaseMidProcessor transferCaseMidProcessor;

    @Mock
    private TransferCasePostProcessor transferCasePostProcessor;

    @Mock
    private CallbackRequest callbackRequest;

    @Mock
    private CallbackResponse expectedResponse;

    @Test
    public void shouldHandleMidCallbackType() {

        CallbackParams callbackParams = getCallbackParams(MID);

        when(transferCaseMidProcessor.generateNoticeOfTransferLetters(callbackParams)).thenReturn(expectedResponse);

        CallbackResponse response = handler.handle(callbackParams);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldHandleAboutToSubmitCallbackType() {

        CallbackParams callbackParams = getCallbackParams(ABOUT_TO_SUBMIT);

        when(transferCasePostProcessor.performBulkPrintTransfer(callbackParams)).thenReturn(expectedResponse);

        CallbackResponse response = handler.handle(callbackParams);

        assertEquals(expectedResponse, response);
    }

    private CallbackParams getCallbackParams(CallbackType callbackType) {
        return CallbackParams.builder()
            .type(callbackType)
            .request(callbackRequest)
            .build();
    }
}
