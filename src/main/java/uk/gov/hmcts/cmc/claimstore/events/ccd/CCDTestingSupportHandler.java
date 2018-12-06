package uk.gov.hmcts.cmc.claimstore.events.ccd;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.repositories.support.CCDTestingSupportRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

public class CCDTestingSupportHandler {
    private final CCDTestingSupportRepository ccdSupportRepository;

    public CCDTestingSupportHandler(CCDTestingSupportRepository ccdSupportRepository) {
        this.ccdSupportRepository = ccdSupportRepository;
    }

    @EventListener
    @Async
    public void updateCCDResponseDeadline(CCDTestingResponseDeadlineEvent event) {
        Claim claim = getCCDClaim(event.getClaimReferenceNumber(), event.getAuthorisation());

        ccdSupportRepository.updateResponseDeadline(event.getAuthorisation(), claim, event.getNewDeadline());
    }

    @EventListener
    @Async
    public void linkDefendantToCCDClaim(CCDTestingLinkDefendantEvent event) {
        Claim claim = getCCDClaim(event.getClaimReferenceNumber(), null);
        ccdSupportRepository.linkDefendantToClaim(claim, event.getDefendantId());
    }

    private Claim getCCDClaim(String claimReferenceNumber, String authorisation) {
        return ccdSupportRepository.getByClaimReferenceNumber(claimReferenceNumber, authorisation)
            .orElseThrow(() -> new NotFoundException("Claim not found by ref no: " + claimReferenceNumber));
    }
}
