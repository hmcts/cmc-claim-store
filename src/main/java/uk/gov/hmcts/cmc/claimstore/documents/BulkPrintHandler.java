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
import uk.gov.hmcts.cmc.claimstore.events.GeneralLetterReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.legaladvisor.DirectionsOrderReadyToPrintEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;

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
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

@Component
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintHandler {

    private final PrintService bulkPrintService;

    @Autowired
    public BulkPrintHandler(PrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @EventListener
    public BulkPrintDetails BulkPrintDetails(DocumentReadyToPrintEvent event) {
        requireNonNull(event);
        Claim claim = event.getClaim();
        return bulkPrintService.print(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    event.getDefendantLetterDocument(),
                    buildDefendantLetterFileBaseName(claim.getReferenceNumber())),
                new PrintableTemplate(
                    event.getSealedClaimDocument(),
                    buildSealedClaimFileBaseName(claim.getReferenceNumber()))
            ),
            event.getAuthorisation()
        );
    }

    @EventListener
    public BulkPrintDetails print(DirectionsOrderReadyToPrintEvent event) {
        requireNonNull(event);
        Claim claim = event.getClaim();
        return bulkPrintService.printPdf(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    event.getCoverSheet(),
                    buildCoverSheetFileBaseName(claim.getReferenceNumber())),
                new PrintablePdf(
                    event.getDirectionsOrder(),
                    buildDirectionsOrderFileBaseName(claim.getReferenceNumber()))
            ),
            BulkPrintRequestType.DIRECTION_ORDER_LETTER_TYPE,
            event.getAuthorisation()
        );
    }

    @EventListener
    public BulkPrintDetails print(GeneralLetterReadyToPrintEvent event) {
        requireNonNull(event);
        Claim claim = event.getClaim();
        return bulkPrintService.printPdf(
            claim,
            ImmutableList.of(
                new PrintablePdf(
                    event.getGeneralLetterDocument(),
                    buildLetterFileBaseName(claim.getReferenceNumber(),
                        String.valueOf(LocalDate.now())))
            ),
            BulkPrintRequestType.GENERAL_LETTER_TYPE,
            event.getAuthorisation()
        );
    }

    @EventListener
    public BulkPrintDetails print(BulkPrintTransferEvent event) {
        requireNonNull(event);
        Claim claim = event.getClaim();

        PrintablePdf coverLetter = new PrintablePdf(
            event.getCoverLetter(),
            buildCoverSheetFileBaseName(claim.getReferenceNumber()));

        List<Printable> printableDocs = new ArrayList<>(List.of(coverLetter));
        printableDocs.addAll(event.getCaseDocuments()
            .stream()
            .map(d -> new PrintablePdf(d.getDocument(), d.getFileName()))
            .collect(Collectors.toList())
        );

        return bulkPrintService.printPdf(claim, Collections.unmodifiableList(printableDocs),
            BulkPrintRequestType.BULK_PRINT_TRANSFER_TYPE, event.getAuthorisation());
    }
}
