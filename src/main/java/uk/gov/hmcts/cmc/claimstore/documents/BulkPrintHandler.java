package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintablePdf;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.events.BulkPrintTransferEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
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
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDirectionsOrderFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildOconFormFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildPaperDefenceCoverLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

@Component
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintHandler {

    private final PrintService bulkPrintService;
    private final LaunchDarklyClient launchDarklyClient;

    @Autowired
    public BulkPrintHandler(PrintService bulkPrintService,
                            LaunchDarklyClient launchDarklyClient) {
        this.bulkPrintService = bulkPrintService;
        this.launchDarklyClient = launchDarklyClient;
    }

    @EventListener
    public BulkPrintDetails print(DocumentReadyToPrintEvent event) {
        requireNonNull(event);
        Claim claim = event.getClaim();
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
                event.getAuthorisation()
            );
        } else {
            bulkPrintDetails = bulkPrintService.printHtmlLetter(claim, List.of(
                new PrintableTemplate(
                    event.getDefendantLetterDocument(),
                    buildDefendantLetterFileBaseName(claim.getReferenceNumber())),
                new PrintableTemplate(
                    event.getSealedClaimDocument(),
                    buildSealedClaimFileBaseName(claim.getReferenceNumber()))
            ), BulkPrintRequestType.FIRST_CONTACT_LETTER_TYPE, event.getAuthorisation());
        }
        return bulkPrintDetails;
    }

    public BulkPrintDetails printDirectionOrder(Claim claim, Document coverSheet,
                                                Document directionsOrder, String authorisation) {
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
            BulkPrintRequestType.DIRECTION_ORDER_LETTER_TYPE,
            authorisation
        );
    }

    public BulkPrintDetails printGeneralLetter(Claim claim, Document generalLetterDocument, String authorisation) {
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
            authorisation
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

        PrintablePdf coverLetterPrint = new PrintablePdf(
            coverLetter,
            buildCoverSheetFileBaseName(claim.getReferenceNumber()));

        List<Printable> printableDocs = new ArrayList<>(List.of(coverLetterPrint));
        printableDocs.addAll(caseDocuments
            .stream()
            .map(d -> new PrintablePdf(d.getDocument(), d.getFileName()))
            .collect(Collectors.toList())
        );

        return bulkPrintService.printPdf(claim, Collections.unmodifiableList(printableDocs),
            BulkPrintRequestType.BULK_PRINT_TRANSFER_TYPE, authorisation);
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
            BulkPrintRequestType.GENERAL_LETTER_TYPE,
            authorisation);
    }
}
