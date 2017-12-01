package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

@Component
public class DocumentGenerator {

    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    private final DefendantPinLetterPdfService defendantPinLetterPdfService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public DocumentGenerator(
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService,
        final DefendantPinLetterPdfService defendantPinLetterPdfService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final ApplicationEventPublisher publisher
    ) {
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
        this.defendantPinLetterPdfService = defendantPinLetterPdfService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.publisher = publisher;
    }

    @EventListener
    public void generateForNonRepresentedClaim(final ClaimIssuedEvent event) {
        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
            citizenSealedClaimPdfService.createPdf(event.getClaim(), event.getSubmitterEmail()));
        PDF defendantLetter = new PDF(buildDefendantLetterFileBaseName(event.getClaim().getReferenceNumber()),
            defendantPinLetterPdfService.createPdf(event.getClaim(), event.getPin()
                .orElseThrow(() -> new IllegalArgumentException("Defendant access PIN is missing"))));

        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(),
            sealedClaim, defendantLetter));
    }

    @EventListener
    public void generateForRepresentedClaim(final RepresentedClaimIssuedEvent event) {
        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
            legalSealedClaimPdfService.createPdf(event.getClaim()));

        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(), sealedClaim));
    }
}
