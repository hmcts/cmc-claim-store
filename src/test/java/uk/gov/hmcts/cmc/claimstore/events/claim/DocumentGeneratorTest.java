package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGeneratorTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    private DocumentGenerator documentGenerator;

    private final String authorisation = "AuthValue";
    private final String submitterName = "Dr. John Smith";
    private final String pin = "123456";
    private final Map<String, Object> pinContents = new HashMap<>();
    private final String pinTemplate = "pinTemplate";
    private final Document defendantLetterDocument = new Document(pinTemplate, pinContents);
    private final Map<String, Object> claimContents = new HashMap<>();
    private final String sealedClaimTemplate = "sealedClaimTemplate";

    @Mock
    private CitizenServiceDocumentsService citizenDocumentService;
    @Mock
    private SealedClaimPdfService sealedClaimPdfService;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private PDFServiceClient pdfServiceClient;
    @Mock
    private ClaimIssueReceiptService claimIssueReceiptService;

    @Before
    public void before() {
        documentGenerator = new DocumentGenerator(citizenDocumentService,
            sealedClaimPdfService,
            publisher,
            pdfServiceClient);
    }

    @Test
    public void shouldPublishDocumentReadyToPrintEventHavingLetterDocumentBeforeClaimDocument() {
        //given

        Document sealedClaimDocument = new Document(sealedClaimTemplate, claimContents);
        Claim claim = SampleClaim.getDefault();
        when(citizenDocumentService.sealedClaimDocument(claim)).thenReturn(sealedClaimDocument);
        when(citizenDocumentService.pinLetterDocument(claim, pin)).thenReturn(defendantLetterDocument);

        when(sealedClaimPdfService.createPdf(claim))
            .thenReturn(new PDF(
                "sealedClaim",
                PDF_CONTENT,
                SEALED_CLAIM
            ));

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

    @Test
    public void shouldTriggerDocumentGeneratedEventForCitizenClaim() {
        Document sealedClaimDocument = new Document(sealedClaimTemplate, claimContents);
        Claim claim = SampleClaim.getDefault();
        when(citizenDocumentService.sealedClaimDocument(claim)).thenReturn(sealedClaimDocument);
        when(citizenDocumentService.pinLetterDocument(claim, pin)).thenReturn(defendantLetterDocument);
        CitizenClaimIssuedEvent event = new CitizenClaimIssuedEvent(claim, pin, submitterName, authorisation);
        documentGenerator.generateForNonRepresentedClaim(event);
        verify(publisher)
            .publishEvent(new DocumentReadyToPrintEvent(claim, defendantLetterDocument, sealedClaimDocument));
        verify(publisher).publishEvent(any(DocumentGeneratedEvent.class));
    }

    @Test
    public void shouldTriggerDocumentGeneratedEventForForRepresentedClaim() {
        Claim claim = SampleClaim.getDefault();
        when(sealedClaimPdfService.createPdf(claim)).thenReturn(new PDF(
            "sealedClaim",
            PDF_CONTENT,
            SEALED_CLAIM
        ));
        RepresentedClaimIssuedEvent event = new RepresentedClaimIssuedEvent(claim, submitterName, authorisation);
        documentGenerator.generateForRepresentedClaim(event);
        verify(publisher).publishEvent(any(DocumentGeneratedEvent.class));
    }
}
