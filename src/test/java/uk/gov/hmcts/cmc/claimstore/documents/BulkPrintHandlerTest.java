package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableList;
import com.launchdarkly.sdk.LDUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintablePdf;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.events.BulkPrintTransferEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.BULK_PRINT_TRANSFER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.CLAIMANT_MEDIATION_REFUSED_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.DIRECTION_ORDER_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.FIRST_CONTACT_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.GENERAL_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterClaimantMediationRefusedFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildOcon9FormFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildOconFormFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildPaperDefenceCoverLetterFileBaseName;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintHandlerTest {

    private static final String AUTHORISATION = "Bearer: let me in";
    private static final List<String> USER_LIST = List.of("Dr. John Smith");
    private static final String DOCUMENT_URL = "document_url";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";
    private static final String DOCUMENT_FILE_NAME = "document_file_name";
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private LaunchDarklyClient launchDarklyClient;
    @Mock
    private PrintableDocumentService printableDocumentService;

    @Test
    public void notifyStaffForNewDefendantLetters() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = SampleClaim.getDefault();
        Document defendantLetterDocument = new Document("pinTemplate", new HashMap<>());
        Document sealedClaimDocument = new Document("sealedClaimTemplate", new HashMap<>());
        when(launchDarklyClient.isFeatureEnabled(eq("new-defendant-pin-letter"), any(LDUser.class))).thenReturn(true);

        DocumentReadyToPrintEvent printEvent
            = new DocumentReadyToPrintEvent(claim, defendantLetterDocument, sealedClaimDocument, AUTHORISATION);

        //when
        bulkPrintHandler.print(printEvent);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    defendantLetterDocument,
                    claim.getReferenceNumber() + "-defendant-pin-letter"),
                new PrintableTemplate(
                    sealedClaimDocument,
                    claim.getReferenceNumber() + "-claim-form")
            ),
            FIRST_CONTACT_LETTER_TYPE,
            AUTHORISATION,
            USER_LIST
        );
    }

    @Test
    public void notifyStaffForDefendantLetters() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = SampleClaim.getDefault();
        Document defendantLetterDocument = new Document("pinTemplate", new HashMap<>());
        Document sealedClaimDocument = new Document("sealedClaimTemplate", new HashMap<>());
        when(launchDarklyClient.isFeatureEnabled(eq("new-defendant-pin-letter"), any(LDUser.class))).thenReturn(false);

        DocumentReadyToPrintEvent printEvent
            = new DocumentReadyToPrintEvent(claim, defendantLetterDocument, sealedClaimDocument, AUTHORISATION);

        //when
        bulkPrintHandler.print(printEvent);

        //verify
        verify(bulkPrintService).printHtmlLetter(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    defendantLetterDocument,
                    claim.getReferenceNumber() + "-defendant-pin-letter"),
                new PrintableTemplate(
                    sealedClaimDocument,
                    claim.getReferenceNumber() + "-claim-form")
            ),
            FIRST_CONTACT_LETTER_TYPE,
            AUTHORISATION,
            USER_LIST
        );
    }

    @Test
    public void notifyStaffForLegalAdvisor() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = SampleClaim.getDefault();
        Document coverSheet = new Document("coverSheet", new HashMap<>());
        Document legalOrder = new Document("legalOrder", new HashMap<>());

        //when
        bulkPrintHandler.printDirectionOrder(claim, coverSheet, legalOrder, AUTHORISATION);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    coverSheet,
                    claim.getReferenceNumber() + "-directions-order-cover-sheet"),
                new PrintablePdf(
                    legalOrder,
                    claim.getReferenceNumber() + "-directions-order")
            ),
            DIRECTION_ORDER_LETTER_TYPE,
            AUTHORISATION,
            USER_LIST);
    }

    @Test
    public void notifyForGeneralLetter() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = SampleClaim.getDefault();
        Document generalLetter = new Document("letter", new HashMap<>());

        //when
        bulkPrintHandler.printGeneralLetter(claim, generalLetter, AUTHORISATION);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.of(
                new PrintablePdf(
                    generalLetter,
                    claim.getReferenceNumber() + "-general-letter-"
                        + LocalDate.now())
            ),
            GENERAL_LETTER_TYPE,
            AUTHORISATION,
            USER_LIST
        );
    }

    @Test
    public void notifyForBulkPrintTransferEvent() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = mock(Claim.class);
        when(claim.getReferenceNumber()).thenReturn("AAA");
        when(claim.getClaimData())
            .thenReturn(
                SampleClaimData
                    .builder()
                    .withDefendant(
                        SampleTheirDetails
                            .builder()
                            .withName("Dr. John Smith")
                            .partyDetails())
                    .build());

        Document coverLetter = new Document("letter", new HashMap<>());
        Document caseDocument = mock(Document.class);
        String caseDocumentFileName = "caseDoc.pdf";

        List<BulkPrintTransferEvent.PrintableDocument> caseDocuments = List.of(
            new BulkPrintTransferEvent.PrintableDocument(caseDocument, caseDocumentFileName)
        );

        //when
        bulkPrintHandler.printBulkTransferDocs(claim, coverLetter, caseDocuments, AUTHORISATION);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            List.of(
                new PrintablePdf(
                    coverLetter,
                    claim.getReferenceNumber() + "-directions-order-cover-sheet"),
                new PrintablePdf(
                    caseDocument,
                    caseDocumentFileName
                )
            ),
            BULK_PRINT_TRANSFER_TYPE,
            AUTHORISATION,
            USER_LIST);
    }

    @Test
    public void notifyPaperDefenceLetter() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = SampleClaim.getDefault();
        Document letter = new Document("letter", new HashMap<>());

        //when
        bulkPrintHandler.printPaperDefence(claim, letter, letter, letter, AUTHORISATION, true);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.<Printable>builder()
                .add(new PrintablePdf(
                    letter,
                    buildPaperDefenceCoverLetterFileBaseName(claim.getReferenceNumber())))
                .add(new PrintablePdf(
                    letter,
                    buildOconFormFileBaseName(claim.getReferenceNumber())))
                .build(),
            GENERAL_LETTER_TYPE,
            AUTHORISATION,
            USER_LIST
        );
    }

    @Test
    public void notifyPaperDefenceLetterWithooutN9() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = SampleClaim.getDefault();
        Document letter = new Document("letter", new HashMap<>());

        //when
        bulkPrintHandler.printPaperDefence(claim, letter, letter, AUTHORISATION);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.<Printable>builder()
                .add(new PrintablePdf(
                    letter,
                    buildPaperDefenceCoverLetterFileBaseName(claim.getReferenceNumber())))
                .add(new PrintablePdf(
                    letter,
                    buildOconFormFileBaseName(claim.getReferenceNumber())))
                .build(),
            GENERAL_LETTER_TYPE,
            AUTHORISATION,
            USER_LIST
        );
    }

    @Test
    public void notifyPaperDefenceLetterWithOCON9() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = SampleClaim.getDefault();
        Document letter = new Document("letter", new HashMap<>());

        //when
        bulkPrintHandler.printPaperDefence(claim, letter, letter, letter, AUTHORISATION, false);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.<Printable>builder()
                .add(new PrintablePdf(
                    letter,
                    buildPaperDefenceCoverLetterFileBaseName(claim.getReferenceNumber())))
                .add(new PrintablePdf(
                    letter,
                    buildOcon9FormFileBaseName(claim.getReferenceNumber())))
                .add(new PrintablePdf(
                    letter,
                    buildOconFormFileBaseName(claim.getReferenceNumber())))
                .build(),
            GENERAL_LETTER_TYPE,
            AUTHORISATION,
            USER_LIST
        );
    }

    @Test
    public void notifyPaperDefenceLetterForClaimantRefusalOCON9x() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = SampleClaim.getSampleClaimantMediationRefusal();
        Document letter = new Document("letter", new HashMap<>());

        //when
        bulkPrintHandler.printClaimantMediationRefusedLetter(claim, AUTHORISATION, letter);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.<Printable>builder()
                .add(new PrintablePdf(
                    letter,
                    buildDefendantLetterClaimantMediationRefusedFileBaseName(claim.getReferenceNumber())))
                .build(),
            CLAIMANT_MEDIATION_REFUSED_TYPE,
            AUTHORISATION,
            USER_LIST
        );
    }

    @Test
    public void notifyDefendantForBulkPrintNoticeOfTransfer() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService, launchDarklyClient, printableDocumentService);
        Claim claim = SampleClaim.getSampleClaimantMediationRefusal();
        CCDDocument ccdDocument = CCDDocument.builder().documentUrl(DOCUMENT_URL)
            .documentBinaryUrl(DOCUMENT_BINARY_URL)
            .documentFileName(DOCUMENT_FILE_NAME)
            .build();

        Document downloadedLetter = printableDocumentService.process(ccdDocument, AUTHORISATION);

        //when
        bulkPrintHandler.printDefendantNoticeOfTransferLetter(claim, ccdDocument, AUTHORISATION);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.<Printable>builder()
                .add(new PrintablePdf(
                    downloadedLetter,
                    buildLetterFileBaseName(claim.getReferenceNumber(), LocalDate.now().toString())))
                .build(),
            BULK_PRINT_TRANSFER_TYPE,
            AUTHORISATION,
            USER_LIST
        );
    }
}
