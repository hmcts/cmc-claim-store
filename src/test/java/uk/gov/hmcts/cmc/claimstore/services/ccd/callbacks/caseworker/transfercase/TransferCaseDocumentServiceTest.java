package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferCaseDocumentServiceTest {

    private static final String CASE_REFERENCE = "0001";
    private static final String AUTHORISATION = "Bearer:auth_token";

    @InjectMocks
    private TransferCaseDocumentService transferCaseDocumentService;

    @Mock
    private GeneralLetterService generalLetterService;

    @Mock
    private CCDCase ccdCase;

    @Mock
    private CCDDocument noticeForCourt;

    @Mock
    private CCDDocument noticeForDefendant;

    @BeforeEach
    public void beforeEach() {
        when(ccdCase.getPreviousServiceCaseReference()).thenReturn(CASE_REFERENCE);
    }

    @Test
    void shouldAttachNoticeOfTransferForCourt() {


        transferCaseDocumentService.attachNoticeOfTransferForCourt(ccdCase, noticeForCourt, AUTHORISATION);

        verify(generalLetterService).attachGeneralLetterToCase(
            eq(ccdCase),
            eq(noticeForCourt),
            eq(CASE_REFERENCE + "-notice-of-transfer-for-court.pdf"),
            eq(AUTHORISATION)

        );
    }

    @Test
    void shouldAttachNoticeOfTransferForDefendant() {

        transferCaseDocumentService.attachNoticeOfTransferForDefendant(ccdCase, noticeForDefendant, AUTHORISATION);

        verify(generalLetterService).attachGeneralLetterToCase(
            eq(ccdCase),
            eq(noticeForDefendant),
            eq(CASE_REFERENCE + "-notice-of-transfer-for-defendant.pdf"),
            eq(AUTHORISATION)
        );
    }
}
