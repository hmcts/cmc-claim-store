package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class TransferCaseCallbackHandlerTest {

    private static final String COURT_NAME = "Bristol";

    @InjectMocks
    private TransferCaseCallbackHandler handler;

    @Mock
    private TransferCasePostProcessor transferCasePostProcessor;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CallbackRequest callbackRequest;

    @Mock
    private CallbackResponse expectedResponse;

    @Mock
    private CCDAddress hearingCourtAddress;

    @Test
    void shouldHandleAboutToStartCallbackTypeForCaseWithoutDirectionOrder() {

        CallbackParams callbackParams = getCallbackParams(ABOUT_TO_START);
        CCDCase ccdCase = getCase(false);

        when(caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails())).thenReturn(ccdCase);

        CallbackResponse response = handler.handle(callbackParams);

        expectedResponse = AboutToStartOrSubmitCallbackResponse.builder().build();

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldHandleAboutToStartCallbackTypeForCaseWithDirectionOrder() {

        CallbackParams callbackParams = getCallbackParams(ABOUT_TO_START);
        CCDCase ccdCase = getCase(true);

        when(caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails())).thenReturn(ccdCase);

        CallbackResponse response = handler.handle(callbackParams);

        expectedResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .data(Map.of(
                "transferContent", CCDTransferContent.builder()
                    .transferCourtName(COURT_NAME)
                    .transferCourtAddress(hearingCourtAddress)
                    .build()
                )
            )
            .build();

        assertEquals(expectedResponse, response);
    }

    private CCDCase getCase(boolean hasDirectionOrder) {

        return hasDirectionOrder
            ? CCDCase.builder()
            .directionOrder(CCDDirectionOrder.builder()
                .hearingCourtName(COURT_NAME)
                .hearingCourtAddress(hearingCourtAddress)
                .build())
            .build()
            : mock(CCDCase.class);
    }

    @Test
    void shouldHandleAboutToSubmitCallbackType() {

        CallbackParams callbackParams = getCallbackParams(ABOUT_TO_SUBMIT);

        when(transferCasePostProcessor.transferToCourt(callbackParams)).thenReturn(expectedResponse);

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
