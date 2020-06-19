package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.CoverLetterGenerator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.TransferCaseLetterSender;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import java.util.Collections;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BulkPrintTransferServiceTest {

    private BulkPrintTransferService bulkPrintTransferService;

    @Mock
    private CaseSearchApi caseSearchApi;
    @Mock
    private UserService userService;
    @Mock
    private TransferCaseLetterSender transferCaseLetterSender;
    @Mock
    private CaseMapper caseMapper;
    @Mock
    private CoverLetterGenerator coverLetterGenerator;

    private static final String COURT_LETTER_TEMPLATE_ID = "courtLetterTemplateId";
    private static final String AUTHORISATION = "authorisation";
    private static final User USER = new User(AUTHORISATION, null);
    private static final Claim SAMPLE_CLAIM = SampleClaim.builder()
        .withResponse(SampleResponse.FullDefence.validDefaults())
        .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
        .build();
    private static final CCDCase SAMPLE_CCD_CASE = CCDCase.builder().id(SampleClaim.CLAIM_ID).build();
    private static final String DOC_NAME = "ref-notice-of-transfer-for-court.pdf";

    private static final CCDDocument NAMED_COVER_DOC = CCDDocument.builder().build().toBuilder()
        .documentFileName(DOC_NAME).build();

    @BeforeEach
    public void beforeEach() {
        bulkPrintTransferService = new BulkPrintTransferService(
            caseSearchApi,
            userService,
            transferCaseLetterSender,
            caseMapper,
            COURT_LETTER_TEMPLATE_ID,
            coverLetterGenerator);

        when(userService.authenticateAnonymousCaseWorker())
            .thenReturn(USER);
        when(caseSearchApi.getClaimsReadyForTransfer(USER))
            .thenReturn(Collections.singletonList(SAMPLE_CLAIM));
        when(caseMapper.to(SAMPLE_CLAIM))
            .thenReturn(SAMPLE_CCD_CASE);
        when(coverLetterGenerator.generate(
            SAMPLE_CCD_CASE,
            AUTHORISATION,
            NoticeOfTransferLetterType.FOR_COURT,
            COURT_LETTER_TEMPLATE_ID))
            .thenReturn(NAMED_COVER_DOC);
        doNothing().when(transferCaseLetterSender)
            .sendAllCaseDocumentsToCourt(AUTHORISATION, SAMPLE_CCD_CASE, SAMPLE_CLAIM, NAMED_COVER_DOC);
    }

    @Test
    void shouldTransferForBulkPrint() {
        bulkPrintTransferService.bulkPrintTransfer();
        verify(userService).authenticateAnonymousCaseWorker();
        verify(caseSearchApi).getClaimsReadyForTransfer(USER);
        verify(coverLetterGenerator).generate(SAMPLE_CCD_CASE,
            AUTHORISATION,
            NoticeOfTransferLetterType.FOR_COURT,
            COURT_LETTER_TEMPLATE_ID);
        verify(transferCaseLetterSender)
            .sendAllCaseDocumentsToCourt(AUTHORISATION, SAMPLE_CCD_CASE, SAMPLE_CLAIM, NAMED_COVER_DOC);
    }
}
