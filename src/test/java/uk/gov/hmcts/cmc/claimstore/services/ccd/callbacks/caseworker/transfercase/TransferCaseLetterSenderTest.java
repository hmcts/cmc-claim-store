package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.events.BulkPrintTransferEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferCaseLetterSenderTest {

    private static final String AUTHORISATION = "Bearer: abcd";

    @InjectMocks
    private TransferCaseLetterSender transferCaseLetterSender;

    @Mock
    private GeneralLetterService generalLetterService;

    @Mock
    private PrintableDocumentService printableDocumentService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private CCDCase ccdCase;

    @Mock
    private Claim claim;

    @Test
    void shouldSendNoticeOfTransferForDefendant() {

        CCDDocument noticeForDefendant = mock(CCDDocument.class);
        when(ccdCase.getDraftLetterDoc()).thenReturn(noticeForDefendant);

        transferCaseLetterSender.sendNoticeOfTransferForDefendant(AUTHORISATION,
            ccdCase, claim);

        verify(generalLetterService).printLetter(
            AUTHORISATION,
            noticeForDefendant,
            claim
        );
    }

    @Test
    void shouldSendAllCaseDocumentsToCourt() {

        CCDDocument noticeForCourt = mock(CCDDocument.class);
        Document coverLetterDoc = mock(Document.class);
        final List<BulkPrintTransferEvent.PrintableDocument> caseDocuments = List.of();

        when(ccdCase.getCoverLetterDoc()).thenReturn(noticeForCourt);
        when(ccdCase.getCaseDocuments()).thenReturn(List.of());
        when(ccdCase.getScannedDocuments()).thenReturn(List.of());
        when(ccdCase.getStaffUploadedDocuments()).thenReturn(List.of());
        when(printableDocumentService.process(noticeForCourt, AUTHORISATION)).thenReturn(coverLetterDoc);

        transferCaseLetterSender.sendAllCaseDocumentsToCourt(AUTHORISATION, ccdCase, claim);

        verify(eventProducer).createBulkPrintTransferEvent(eq(claim), eq(coverLetterDoc), eq(caseDocuments));
    }
}
