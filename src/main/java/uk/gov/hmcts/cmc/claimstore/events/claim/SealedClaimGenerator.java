package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.events.SealedClaimGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;

@Component
public class SealedClaimGenerator {

    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public SealedClaimGenerator(final LegalSealedClaimPdfService legalSealedClaimPdfService,
                                final ApplicationEventPublisher publisher) {
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.publisher = publisher;
    }

    @EventListener
    public void generateForRepresentedClaim(final RepresentedClaimIssuedEvent event) {
        byte[] document = legalSealedClaimPdfService.createPdf(event.getClaim());
        publisher.publishEvent(new SealedClaimGeneratedEvent(event.getClaim(), event.getAuthorisation(), document));
    }
}
