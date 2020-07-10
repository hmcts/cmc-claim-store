package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
    private CallbackRequest callbackRequest;

    @Mock
    private BulkPrintTransferService bulkPrintTransferService;

    @Mock
    private Claim claim;

    private CCDCase ccdCase;

    @BeforeEach
    public void beforeEach() {
        ccdCase = SampleData.getCCDLegalCase();
    }

    @Test
    void shouldCompleteCaseTransferForLinkedDefendants() {

        givenDefendantIsLinked(true);

        Map<String, Object> mappedCaseData = mock(Map.class);
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(mappedCaseData);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(caseDetails)).thenReturn(claim);

        when(bulkPrintTransferService.transferCase(ccdCase, claim, AUTHORISATION))
            .thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse)
            transferCasePostProcessor.completeCaseTransfer(callbackParams);

        verify(bulkPrintTransferService).transferCase(ccdCase, claim, AUTHORISATION);

        assertEquals(mappedCaseData, callbackResponse.getData());
    }

    private void givenDefendantIsLinked(boolean isLinked) {

        CCDRespondent.CCDRespondentBuilder defendantBuilder = CCDRespondent.builder();

        if (isLinked) {
            defendantBuilder.defendantId(DEFENDANT_ID);
        }

        List<CCDCollectionElement<CCDRespondent>> respondents
            = singletonList(CCDCollectionElement.<CCDRespondent>builder().value(
            defendantBuilder.build()).build());

        ccdCase = ccdCase.toBuilder().respondents(respondents)
            .transferContent(CCDTransferContent.builder().build())
            .build();
    }
}
