package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferReason;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.BulkPrintTransferService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.TransferCaseDocumentPublishService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.TransferCaseNotificationsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkPrintTransferServiceTest {

    private BulkPrintTransferService bulkPrintTransferService;

    @Mock
    private CaseSearchApi caseSearchApi;
    @Mock
    private UserService userService;
    @Mock
    private CaseMapper caseMapper;
    @Mock
    private TransferCaseDocumentPublishService transferCaseDocumentPublishService;
    @Mock
    private TransferCaseNotificationsService transferCaseNotificationsService;
    @Mock
    private CoreCaseDataService coreCaseDataService;

    private static final String HEARING_COURT_NAME = "court";
    private static final CCDAddress HEARING_COURT_ADDRESS = CCDAddress.builder().build();
    private static final String AUTHORISATION = "authorisation";
    private static final String REASON = "For directions";
    private static final User USER = new User(AUTHORISATION, null);
    private static final Claim SAMPLE_CLAIM = SampleClaim.builder()
        .withResponse(SampleResponse.FullDefence.validDefaults())
        .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
        .build();
    private CCDCase sampleCcdCase = SampleData.getCCDLegalCase()
        .toBuilder()
        .hearingCourtName(HEARING_COURT_NAME)
        .hearingCourtAddress(HEARING_COURT_ADDRESS)
        .build();
    private CCDCase transferredCCDCase;
    private static final CaseDetails SAMPLE_CASE_DETAILS = CaseDetails.builder().id(SampleClaim.CLAIM_ID).build();

    @BeforeEach
    public void beforeEach() {
        CCDTransferContent transferContent = CCDTransferContent.builder()
            .dateOfTransfer(LocalDate.now())
            .transferCourtName(HEARING_COURT_NAME)
            .transferCourtAddress(HEARING_COURT_ADDRESS)
            .transferReason(CCDTransferReason.OTHER)
            .transferReasonOther(REASON).build();
        transferredCCDCase = sampleCcdCase.toBuilder()
            .state(ClaimState.TRANSFERRED.getValue())
            .transferContent(transferContent)
            .build();

        bulkPrintTransferService = new BulkPrintTransferService(
            caseSearchApi,
            userService,
            caseMapper,
            transferCaseDocumentPublishService,
            transferCaseNotificationsService,
            coreCaseDataService);
        when(transferCaseDocumentPublishService
            .publishCaseDocuments(sampleCcdCase, AUTHORISATION, SAMPLE_CLAIM))
            .thenReturn(sampleCcdCase);
        doNothing().when(transferCaseNotificationsService)
            .sendClaimUpdatedEmailToClaimant(SAMPLE_CLAIM);
    }

    @Test
    void shouldFindCasesAndTransfer() {
        when(userService.authenticateAnonymousCaseWorker())
            .thenReturn(USER);
        when(caseSearchApi.getClaimsReadyForTransfer(USER))
            .thenReturn(Collections.singletonList(SAMPLE_CLAIM));
        when(caseMapper.to(SAMPLE_CLAIM))
            .thenReturn(sampleCcdCase);
        when(coreCaseDataService.update(AUTHORISATION, transferredCCDCase, CaseEvent.AUTOMATED_TRANSFER))
            .thenReturn(SAMPLE_CASE_DETAILS);
        bulkPrintTransferService.findCasesAndTransfer();
        verify(userService).authenticateAnonymousCaseWorker();
        verify(caseSearchApi).getClaimsReadyForTransfer(USER);
        verify(transferCaseDocumentPublishService)
            .publishCaseDocuments(sampleCcdCase, AUTHORISATION, SAMPLE_CLAIM);
        verify(transferCaseNotificationsService)
            .sendClaimUpdatedEmailToClaimant(SAMPLE_CLAIM);
        verify(coreCaseDataService)
            .update(AUTHORISATION, transferredCCDCase, CaseEvent.AUTOMATED_TRANSFER);
    }

    @Test
    void shouldTransferCase() {
        CCDTransferContent transferContent = CCDTransferContent.builder()
            .dateOfTransfer(LocalDate.now()).build();
        CCDCase ccdCaseExpected = sampleCcdCase.toBuilder().transferContent(transferContent).build();
        CCDCase ccdCase = bulkPrintTransferService.transferCase(sampleCcdCase, SAMPLE_CLAIM, AUTHORISATION);
        verify(transferCaseDocumentPublishService)
            .publishCaseDocuments(sampleCcdCase, AUTHORISATION, SAMPLE_CLAIM);
        verify(transferCaseNotificationsService)
            .sendClaimUpdatedEmailToClaimant(SAMPLE_CLAIM);
        assertEquals(ccdCaseExpected, ccdCase);
    }
}
