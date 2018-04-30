package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGeneratorTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    private DocumentGenerator documentGenerator;

    @Mock
    private CitizenServiceDocumentsService citizenDocumentService;
    @Mock
    private SealedClaimPdfService claimPdfService;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private PDFServiceClient pdfServiceClient;

    @Before
    public void before() {
        documentGenerator = new DocumentGenerator(citizenDocumentService, claimPdfService, publisher, pdfServiceClient);
    }

    @Test
    public void shouldPublishDocumentReadyToPrintEventHavingLetterDocumentBeforeClaimDocument() {
        //given
        String authorisation = "AuthValue";
        String pin = "123456";
        String submitterName = "Dr. John Smith";
        Map<String, Object> pinContents = new HashMap<>();
        String pinTemplate = "pinTemplate";
        Document defendantLetterDocument = new Document(pinTemplate, pinContents);
        Map<String, Object> claimContents = new HashMap<>();
        String sealedClaimTemplate = "sealedClaimTemplate";
        Document sealedClaimDocument = new Document(sealedClaimTemplate, claimContents);
        Claim claim = SampleClaim.getDefault();
        when(citizenDocumentService.sealedClaimDocument(claim)).thenReturn(sealedClaimDocument);
        when(citizenDocumentService.pinLetterDocument(claim, pin)).thenReturn(defendantLetterDocument);

        when(claimPdfService.createPdf(claim))
            .thenReturn(PDF_CONTENT);

        when(pdfServiceClient.generateFromHtml(pinTemplate.getBytes(), defendantLetterDocument.values))
            .thenReturn(PDF_CONTENT);

        //when
        CitizenClaimIssuedEvent event = new CitizenClaimIssuedEvent(claim, pin, submitterName, authorisation);
        documentGenerator.generateForNonRepresentedClaim(event);

        //then
        verify(publisher, never())
            .publishEvent(new DocumentReadyToPrintEvent(claim, sealedClaimDocument, defendantLetterDocument));

        verify(publisher)
            .publishEvent(new DocumentReadyToPrintEvent(claim, defendantLetterDocument, sealedClaimDocument));
    }
}
