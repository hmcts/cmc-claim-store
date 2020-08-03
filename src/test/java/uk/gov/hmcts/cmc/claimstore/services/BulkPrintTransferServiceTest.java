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
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static java.time.LocalDate.now;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent.builder;

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
        CCDTransferContent transferContent = builder()
            .dateOfTransfer(now())
            .transferCourtName(HEARING_COURT_NAME)
            .transferCourtAddress(HEARING_COURT_ADDRESS)
            .transferReason(CCDTransferReason.OTHER)
            .transferReasonOther(REASON).build();
        transferredCCDCase = sampleCcdCase.toBuilder()
            .transferContent(transferContent)
            .build();

        bulkPrintTransferService = new BulkPrintTransferService(
            caseSearchApi,
            userService,
            caseMapper,
            transferCaseDocumentPublishService,
            transferCaseNotificationsService,
            coreCaseDataService);
    }

    @Test
    void shouldFindCasesAndTransfer() {
        when(userService.authenticateAnonymousCaseWorker())
            .thenReturn(USER);
        CCDCase caseWithHearingCourt = sampleCcdCase.toBuilder().hearingCourt(HEARING_COURT_NAME)
            .hearingCourtAddress(HEARING_COURT_ADDRESS).build();
        when(caseSearchApi.getClaimsReadyForTransfer(USER, "data.hearingCourtName", "data.hearingCourtAddress"))
            .thenReturn(Collections.singletonList(caseWithHearingCourt));
       when(caseMapper.to(SAMPLE_CLAIM))
            .thenReturn(sampleCcdCase);
        CCDCase caseWithHearingCourtWithTransferContent = addTransferContent(caseWithHearingCourt);
        lenient().when(caseMapper.from(addTransferContent(caseWithHearingCourtWithTransferContent)))
            .thenReturn(SAMPLE_CLAIM);
        when(coreCaseDataService.update(AUTHORISATION, transferredCCDCase, CaseEvent.AUTOMATED_TRANSFER))
            .thenReturn(SAMPLE_CASE_DETAILS);
        lenient().when(transferCaseDocumentPublishService
            .publishCaseDocuments(caseWithHearingCourtWithTransferContent, AUTHORISATION, SAMPLE_CLAIM))
            .thenReturn(caseWithHearingCourtWithTransferContent);
        doNothing().when(transferCaseNotificationsService)
            .sendTransferToCourtEmail(caseWithHearingCourtWithTransferContent, SAMPLE_CLAIM);
        bulkPrintTransferService.findCasesAndTransfer();
        verify(userService).authenticateAnonymousCaseWorker();
        verify(caseSearchApi).getClaimsReadyForTransfer(USER, "data.hearingCourtName", "data.hearingCourtAddress");
        verify(transferCaseDocumentPublishService)
            .publishCaseDocuments(caseWithHearingCourtWithTransferContent, AUTHORISATION, SAMPLE_CLAIM);
        verify(transferCaseNotificationsService)
            .sendTransferToCourtEmail(caseWithHearingCourtWithTransferContent, SAMPLE_CLAIM);
        verify(coreCaseDataService)
            .update(AUTHORISATION, bulkPrintTransferService
                .updateCaseData(caseWithHearingCourtWithTransferContent), CaseEvent.AUTOMATED_TRANSFER);
    }

    private CCDCase addTransferContent(CCDCase ccdCase){
        CCDTransferContent transferContent = CCDTransferContent.builder()
            .transferCourtName(HEARING_COURT_NAME)
            .transferCourtAddress(HEARING_COURT_ADDRESS)
            .transferReason(CCDTransferReason.OTHER)
            .transferReasonOther(REASON)
            .build();

        return  ccdCase.toBuilder()
            .transferContent(transferContent)
            .build();
    }

    @Test
    void updateCaseDataWithHandOffDate() {
        CCDCase ccdCase = bulkPrintTransferService.updateCaseDataWithHandOffDate(sampleCcdCase);
        assertEquals(now(), ccdCase.getDateOfHandoff());
    }

    @Test
    void shouldUpdateCaseData() {
        CCDCase ccdCase = bulkPrintTransferService.updateCaseData(sampleCcdCase);
        assertEquals(now(), ccdCase.getTransferContent().getDateOfTransfer());

        CCDTransferContent transferContent = builder().dateOfTransfer(now()).build();
        CCDCase ccdCaseWithTransferContent = sampleCcdCase.toBuilder().transferContent(transferContent).build();
        ccdCase = bulkPrintTransferService.updateCaseData(ccdCaseWithTransferContent);
        assertEquals(now(), ccdCase.getTransferContent().getDateOfTransfer());
    }
}
