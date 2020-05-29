package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.MID;

@ExtendWith(MockitoExtension.class)
public class TransferCaseCallbackHandlerTest {

    private TransferCaseCallbackHandler handler;

    @Mock
    private TransferCaseMidProcessor transferCaseMidProcessor;

    @Mock
    private TransferCasePostProcessor transferCasePostProcessor;

    @Mock
    private CallbackRequest callbackRequest;

    @Mock
    private CallbackResponse expectedResponse;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Test
    public void shouldHandleMidCallbackType() {

        givenBulkPrintTransferFeatureEnabled(false);

        CallbackParams callbackParams = getCallbackParams(MID);

        when(transferCaseMidProcessor.generateNoticeOfTransferLetters(callbackParams)).thenReturn(expectedResponse);

        CallbackResponse response = handler.handle(callbackParams);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldHandleAboutToSubmitCallbackType() {

        givenBulkPrintTransferFeatureEnabled(true);

        CallbackParams callbackParams = getCallbackParams(ABOUT_TO_SUBMIT);

        when(transferCasePostProcessor.completeCaseTransfer(callbackParams)).thenReturn(expectedResponse);

        CallbackResponse response = handler.handle(callbackParams);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldRegisterHandlerIfBulkPrintFeatureIsEnabled() {

        givenBulkPrintTransferFeatureEnabled(true);

        Map<String, CallbackHandler> handlers = new HashMap<>();

        handler.register(handlers);

        assertEquals(1, handlers.size());
    }

    @Test
    public void shouldNotRegisterHandlerIfBulkPrintFeatureIsDisabled() {

        givenBulkPrintTransferFeatureEnabled(false);

        Map<String, CallbackHandler> handlers = new HashMap<>();

        handler.register(handlers);

        assertEquals(0, handlers.size());
    }

    private CallbackParams getCallbackParams(CallbackType callbackType) {
        return CallbackParams.builder()
            .type(callbackType)
            .request(callbackRequest)
            .build();
    }

    private void givenBulkPrintTransferFeatureEnabled(boolean enabled) {
        handler = new TransferCaseCallbackHandler(transferCaseMidProcessor, transferCasePostProcessor,
            caseDetailsConverter, enabled);
    }
}
