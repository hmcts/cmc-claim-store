package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static java.time.LocalDate.now;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferCasePostProcessorTest {

    private static final String AUTHORISATION = "Bearer: abcd";
    private static final String DEFENDANT_ID = "4";

    @InjectMocks
    private TransferCasePostProcessor transferCasePostProcessor;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private TransferCaseDocumentPublishService transferCaseDocumentPublishService;

    @Mock
    private TransferCaseNotificationsService transferCaseNotificationsService;

    @Mock
    private CallbackRequest callbackRequest;

    @Mock
    private Claim claim;

    @Mock
    private CCDCase ccdCase;

    @Captor
    ArgumentCaptor<CCDCase> ccdCaptor;

    private CallbackParams callbackParams;

    @BeforeEach
    public void beforeEach() {

        ccdCase = SampleData.getCCDLegalCase();

        callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();

        Map<String, Object> mappedCaseData = mock(Map.class);
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(mappedCaseData);

        ccdCase = ccdCase.toBuilder().transferContent(CCDTransferContent.builder().build()).build();

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(caseDetails)).thenReturn(claim);

        lenient().when(transferCaseDocumentPublishService.publishCaseDocuments(ccdCase, AUTHORISATION, claim))
            .thenReturn(ccdCase);
        lenient().when(transferCaseDocumentPublishService.publishDefendentDocuments(ccdCase, AUTHORISATION, claim))
            .thenReturn(ccdCase);

    }

    @Test
    void shouldTransferCaseToCcbcAndUpdateTheHandOffDate() {

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse)
            transferCasePostProcessor.transferToCCBC(callbackParams);

        verify(transferCaseNotificationsService).sendTransferToCcbcEmail(ccdCase, claim);
        verify(transferCaseDocumentPublishService).publishDefendentDocuments(ccdCase, AUTHORISATION, claim);
        verify(caseDetailsConverter).convertToMap(ccdCaptor.capture());

        assertEquals(now().toString(), ccdCaptor.getValue().getDateOfHandoff().toString());
    }

    @Test
    void shouldCompleteCaseTransferForLinkedDefendants() {

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse)
            transferCasePostProcessor.transferToCourt(callbackParams);

        verify(transferCaseNotificationsService).sendTransferToCourtEmail(ccdCase, claim);
        verify(transferCaseDocumentPublishService).publishCaseDocuments(ccdCase, AUTHORISATION, claim);
        verify(caseDetailsConverter).convertToMap(ccdCaptor.capture());

        assertEquals(now().toString(), ccdCaptor.getValue().getTransferContent().getDateOfTransfer().toString());
    }
}
