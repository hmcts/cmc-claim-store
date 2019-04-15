package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.events.operations.BulkPrintOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.DefendantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.NotifyStaffOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RepresentativeOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.UploadOperationService;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClaimCreatedOperationHandlerTest {
    public static final Claim claim = SampleClaim.getDefault();
    public static final String pin = "pin";
    public static final String submitterName = "submitter-name";
    public static final String authorisation = "authorisation";
    private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};

    private Map<String, Object> pinContents = new HashMap<>();
    private String pinTemplate = "pinTemplate";
    private Document defendantLetterDocument = new Document(pinTemplate, pinContents);

    private Map<String, Object> claimContents = new HashMap<>();
    private String claimTemplate = "claimTemplate";
    private Document sealedClaimLetterDocument = new Document(claimTemplate, claimContents);

    private ClaimCreatedOperationHandler claimCreatedOperationHandler;
    @Mock
    private BulkPrintOperationService bulkPrintOperationService;
    @Mock
    private CitizenServiceDocumentsService citizenServiceDocumentsService;
    @Mock
    private SealedClaimPdfService sealedClaimPdfService;
    @Mock
    private PDFServiceClient pdfServiceClient;
    @Mock
    private ClaimIssueReceiptService ClaimIssueReceiptService;
    @Mock
    private RepresentativeOperationService representativeOperationService;
    @Mock
    private ClaimantOperationService claimantOperationService;
    @Mock
    private DefendantOperationService defendantOperationService;
    @Mock
    private RpaOperationService rpaOperationService;
    @Mock
    private NotifyStaffOperationService notifyStaffOperationService;
    @Mock
    private UploadOperationService uploadOperationService;

    @Before
    public void before() {
        claimCreatedOperationHandler = new ClaimCreatedOperationHandler(
            citizenServiceDocumentsService,
            sealedClaimPdfService,
            pdfServiceClient,
            ClaimIssueReceiptService,
            representativeOperationService,
            bulkPrintOperationService,
            claimantOperationService,
            defendantOperationService,
            rpaOperationService,
            notifyStaffOperationService,
            uploadOperationService
        );

        given(citizenServiceDocumentsService.pinLetterDocument(eq(claim), eq(pin))).willReturn(defendantLetterDocument);
        given(citizenServiceDocumentsService.sealedClaimDocument(eq(claim))).willReturn(sealedClaimLetterDocument);
        given(sealedClaimPdfService.createPdf(eq(claim))).willReturn(PDF_BYTES);
        given(pdfServiceClient.generateFromHtml(any(), anyMap())).willReturn(PDF_BYTES);
        given(ClaimIssueReceiptService.createPdf(eq(claim))).willReturn(PDF_BYTES);
        given(representativeOperationService.notify(eq(claim), eq(submitterName), eq(authorisation))).willReturn(claim);
        given(bulkPrintOperationService.print(eq(claim), any(), any(), eq(authorisation))).willReturn(claim);
        given(claimantOperationService.notifyCitizen(eq(claim), any(), eq(authorisation))).willReturn(claim);
        given(defendantOperationService.notify(eq(claim), any(), any(), eq(authorisation))).willReturn(claim);
        given(rpaOperationService.notify(eq(claim), eq(authorisation), any())).willReturn(claim);
        given(notifyStaffOperationService.notify(eq(claim), eq(authorisation), any())).willReturn(claim);
        given(uploadOperationService.uploadDocument(eq(claim), eq(authorisation), any())).willReturn(claim);

    }

    @Test
    public void citizenIssueHandler() {
        //given
        CitizenClaimCreatedEvent event = new CitizenClaimCreatedEvent(claim, pin, submitterName, authorisation);

        //when
        claimCreatedOperationHandler.citizenIssueHandler(event);

        //then
        verify(citizenServiceDocumentsService).sealedClaimDocument(eq(claim));
        verify(pdfServiceClient).generateFromHtml(any(), anyMap());
        verify(ClaimIssueReceiptService).createPdf(eq(claim));
        verify(bulkPrintOperationService).print(eq(claim), any(), any(), eq(authorisation));
        verify(claimantOperationService).notifyCitizen(eq(claim), any(), eq(authorisation));
        verify(defendantOperationService).notify(eq(claim), any(), any(), eq(authorisation));
        verify(rpaOperationService).notify(eq(claim), eq(authorisation), any());
        verify(notifyStaffOperationService).notify(eq(claim), eq(authorisation), any());
        verify(uploadOperationService, atLeast(3)).uploadDocument(eq(claim), eq(authorisation), any());

    }

    @Test
    public void representativeIssueHandler() {
        //given
        RepresentedClaimCreatedEvent event = new RepresentedClaimCreatedEvent(claim, submitterName, authorisation);

        //when
        claimCreatedOperationHandler.representativeIssueHandler(event);

        //then
        verify(sealedClaimPdfService).createPdf(eq(claim));
        verify(representativeOperationService).notify(eq(claim), eq(submitterName), eq(authorisation));

        verify(claimantOperationService)
            .confirmRepresentative(eq(claim), eq(submitterName), anyString(), eq(authorisation));

        verify(rpaOperationService).notify(eq(claim), eq(authorisation), any());
        verify(notifyStaffOperationService).notify(eq(claim), eq(authorisation), any());
        verify(uploadOperationService).uploadDocument(eq(claim), eq(authorisation), any());
    }
}
