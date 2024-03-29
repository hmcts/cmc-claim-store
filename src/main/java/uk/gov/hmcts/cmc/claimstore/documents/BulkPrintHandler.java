package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintablePdf;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.events.BulkPrintTransferEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDataExtractorUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildCoverSheetFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterClaimantMediationRefusedFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDirectionsOrderFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildOcon9FormFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildOconFormFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildPaperDefenceCoverLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.utils.OCON9xResponseUtil.defendantFullDefenceMediationOCON9x;

@Component
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintHandler {

    private final PrintService bulkPrintService;
    private final LaunchDarklyClient launchDarklyClient;
    private final PrintableDocumentService printableDocumentService;

    @Autowired
    public BulkPrintHandler(PrintService bulkPrintService,
                            LaunchDarklyClient launchDarklyClient,
                            PrintableDocumentService printableDocumentService) {
        this.bulkPrintService = bulkPrintService;
        this.launchDarklyClient = launchDarklyClient;
        this.printableDocumentService = printableDocumentService;
    }

    @EventListener
    public BulkPrintDetails print(DocumentReadyToPrintEvent event) {
        requireNonNull(event);
        var claim = event.getClaim();
        BulkPrintDetails bulkPrintDetails;
        if (launchDarklyClient.isFeatureEnabled("new-defendant-pin-letter", LaunchDarklyClient.CLAIM_STORE_USER)) {
            bulkPrintDetails = bulkPrintService.printPdf(claim, List.of(
                new PrintableTemplate(
                    event.getDefendantLetterDocument(),
                    buildDefendantLetterFileBaseName(claim.getReferenceNumber())),
                new PrintableTemplate(
                    event.getSealedClaimDocument(),
                    buildSealedClaimFileBaseName(claim.getReferenceNumber()))
                ),
                BulkPrintRequestType.FIRST_CONTACT_LETTER_TYPE,
                event.getAuthorisation(),
                CaseDataExtractorUtils.getDefendant(claim)
            );
        } else {
            bulkPrintDetails = bulkPrintService.printHtmlLetter(claim, List.of(
                new PrintableTemplate(
                    event.getDefendantLetterDocument(),
                    buildDefendantLetterFileBaseName(claim.getReferenceNumber())
                ),
                new PrintableTemplate(
                    event.getSealedClaimDocument(),
                    buildSealedClaimFileBaseName(claim.getReferenceNumber()))
                ),
                BulkPrintRequestType.FIRST_CONTACT_LETTER_TYPE,
                event.getAuthorisation(),
                CaseDataExtractorUtils.getDefendant(claim));
        }
        return bulkPrintDetails;
    }

    public BulkPrintDetails printClaimantMediationRefusedLetter(Claim claim, String authorisation, Document document) {
        requireNonNull(authorisation);
        requireNonNull(claim);
        requireNonNull(document);

        BulkPrintDetails bulkPrintDetails = null;
        if (defendantFullDefenceMediationOCON9x(claim)) {
            bulkPrintDetails = bulkPrintService.printPdf(claim, List.of(
                    new PrintablePdf(
                        document,
                        buildDefendantLetterClaimantMediationRefusedFileBaseName(
                            claim.getReferenceNumber()))
                ),
                BulkPrintRequestType.CLAIMANT_MEDIATION_REFUSED_TYPE,
                authorisation,
                CaseDataExtractorUtils.getDefendant(claim));
        }
        return bulkPrintDetails;
    }

    public BulkPrintDetails printDirectionOrder(Claim claim, Document coverSheet,
                                                Document directionsOrder, String authorisation,
                                                List<String> userList) {
        requireNonNull(claim);
        requireNonNull(coverSheet);
        requireNonNull(directionsOrder);
        requireNonNull(authorisation);
        return bulkPrintService.printPdf(
            claim,
            List.of(
                new PrintableTemplate(
                    coverSheet,
                    buildCoverSheetFileBaseName(claim.getReferenceNumber())),
                new PrintablePdf(
                    directionsOrder,
                    buildDirectionsOrderFileBaseName(claim.getReferenceNumber()))
            ),
            BulkPrintRequestType.DIRECTION_ORDER_LETTER_TYPE, authorisation, userList
        );
    }

    public BulkPrintDetails printDefendantNoticeOfTransferLetter(Claim claim,
                                                                 CCDDocument ccdDocument,
                                                                 String authorisation) {
        requireNonNull(claim);
        requireNonNull(ccdDocument);
        requireNonNull(authorisation);

        Document downloadedLetter = printableDocumentService.process(ccdDocument, authorisation);

        return bulkPrintService.printPdf(
            claim,
            List.of(
                new PrintablePdf(
                    downloadedLetter,
                    buildLetterFileBaseName(claim.getReferenceNumber(),
                        String.valueOf(LocalDate.now())))
            ),
            BulkPrintRequestType.BULK_PRINT_TRANSFER_TYPE,
            authorisation,
            CaseDataExtractorUtils.getDefendantForBulkPrint(claim)
        );
    }

    public BulkPrintDetails printGeneralLetter(Claim claim, Document generalLetterDocument, String authorisation, List<String> personList) {
        requireNonNull(claim);
        requireNonNull(generalLetterDocument);
        requireNonNull(authorisation);

        return bulkPrintService.printPdf(
            claim,
            List.of(
                new PrintablePdf(
                    generalLetterDocument,
                    buildLetterFileBaseName(claim.getReferenceNumber(),
                        String.valueOf(LocalDate.now())))
            ),
            BulkPrintRequestType.GENERAL_LETTER_TYPE,
            authorisation,
            personList
        );
    }

    public BulkPrintDetails printBulkTransferDocs(Claim claim,
                                                  Document coverLetter,
                                                  List<BulkPrintTransferEvent.PrintableDocument> caseDocuments,
                                                  String authorisation) {
        requireNonNull(claim);
        requireNonNull(coverLetter);
        requireNonNull(authorisation);
        requireNonNull(caseDocuments);

        var coverLetterPrint = new PrintablePdf(
            coverLetter,
            buildCoverSheetFileBaseName(claim.getReferenceNumber()));

        List<Printable> printableDocs = new ArrayList<>(List.of(coverLetterPrint));
        printableDocs.addAll(caseDocuments
            .stream()
            .map(d -> new PrintablePdf(d.getDocument(), d.getFileName()))
            .collect(Collectors.toList())
        );

        return bulkPrintService.printPdf(claim, Collections.unmodifiableList(printableDocs),
            BulkPrintRequestType.BULK_PRINT_TRANSFER_TYPE, authorisation, CaseDataExtractorUtils.getDefendantForBulkPrint(claim));
    }

    public BulkPrintDetails printPaperDefence(Claim claim, Document coverLetter, Document oconForm,
                                              String authorisation) {
        requireNonNull(claim);
        requireNonNull(coverLetter);
        requireNonNull(authorisation);
        requireNonNull(oconForm);

        return bulkPrintService.printPdf(claim,
            ImmutableList.<Printable>builder()
                .add(new PrintablePdf(
                    coverLetter,
                    buildPaperDefenceCoverLetterFileBaseName(claim.getReferenceNumber())))
                .add(new PrintablePdf(
                    oconForm,
                    buildOconFormFileBaseName(claim.getReferenceNumber())))
                .build(),
            BulkPrintRequestType.PAPER_DEFENCE_TYPE,
            authorisation,
            CaseDataExtractorUtils.getDefendant(claim));
    }

    public BulkPrintDetails printPaperDefence(Claim claim, Document coverLetter, Document oconForm, Document ocon9Form,
                                              String authorisation, boolean disableN9Form) {
        requireNonNull(claim);
        requireNonNull(coverLetter);
        requireNonNull(authorisation);
        requireNonNull(oconForm);
        ImmutableList<Printable> documents;
        documents = getPrintables(claim, coverLetter, oconForm, ocon9Form, disableN9Form);
        return bulkPrintService.printPdf(claim,
            documents,
            BulkPrintRequestType.PAPER_DEFENCE_TYPE,
            authorisation,
            CaseDataExtractorUtils.getDefendant(claim));
    }

    private ImmutableList<Printable> getPrintables(Claim claim, Document coverLetter, Document oconForm,
                                                   Document ocon9Form, boolean disableN9Form) {
        ImmutableList<Printable> documents;
        if (disableN9Form) {
            documents = ImmutableList.<Printable>builder()
                .add(new PrintablePdf(
                    coverLetter,
                    buildPaperDefenceCoverLetterFileBaseName(claim.getReferenceNumber())))
                .add(new PrintablePdf(
                    oconForm,
                    buildOconFormFileBaseName(claim.getReferenceNumber())))
                .build();
        } else {
            documents = ImmutableList.<Printable>builder()
                .add(new PrintablePdf(
                    coverLetter,
                    buildPaperDefenceCoverLetterFileBaseName(claim.getReferenceNumber())))
                .add(new PrintablePdf(
                    ocon9Form,
                    buildOcon9FormFileBaseName(claim.getReferenceNumber())))
                .add(new PrintablePdf(
                    oconForm,
                    buildOconFormFileBaseName(claim.getReferenceNumber())))
                .build();
        }
        return documents;
    }
}
