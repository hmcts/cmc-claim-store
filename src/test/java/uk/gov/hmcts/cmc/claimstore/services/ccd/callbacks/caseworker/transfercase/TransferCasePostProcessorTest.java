package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.elasticsearch.common.TriFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

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
    private BulkPrintTransferService bulkPrintTransferService;

    @Mock
    private PilotCourtService pilotCourtService;

    @Mock
    private DirectionOrderService directionOrderService;

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
        lenient().doReturn(mappedCaseData).when(caseDetailsConverter).convertToMap(any(CCDCase.class));

        ccdCase = ccdCase.toBuilder().transferContent(CCDTransferContent.builder().build()).build();

        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(caseDetails)).thenReturn(Claim.builder().build());

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);

        when(bulkPrintTransferService.transferCase(any(CCDCase.class), any(Claim.class), any(String.class),
            any(TriFunction.class), any(BiConsumer.class), any(UnaryOperator.class))).thenReturn(ccdCase);

    }

    @Test
    void shouldTransferCaseToCcbcAndUpdateTheHandOffDate() {

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse)
            transferCasePostProcessor.transferToCCBC(callbackParams);

        verify(bulkPrintTransferService).transferCase(any(CCDCase.class), any(Claim.class), any(String.class),
            any(TriFunction.class), any(BiConsumer.class), any(UnaryOperator.class));

    }

    @Test
    void shouldCompleteCaseTransferForLinkedDefendants() {

        when(directionOrderService.getHearingCourt(any()))
            .thenReturn(HearingCourt.builder()
                .name("Birmingham Court")
                .address(CCDAddress.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .addressLine3("line3")
                    .postCode("SW1P4BB")
                    .postTown("Birmingham")
                    .build()
                )
                .build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse)
            transferCasePostProcessor.transferToCourt(callbackParams);

        verify(bulkPrintTransferService).transferCase(any(CCDCase.class), any(Claim.class), any(String.class),
            any(TriFunction.class), any(BiConsumer.class), any(UnaryOperator.class));
    }
}
