package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static java.util.Objects.requireNonNull;

@Component
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentUploadHandler {

    private static final String CLAIM_MUST_NOT_BE_NULL = "Claim must not be null";

    private final DocumentsService documentService;

    @Autowired
    public DocumentUploadHandler(DocumentsService documentService) {
        this.documentService = documentService;
    }

    @EventListener
    public void uploadDocument(CitizenClaimIssuedEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateSealedClaim(claim.getExternalId(), event.getAuthorisation());
        documentService.generateClaimIssueReceipt(claim.getExternalId(), event.getAuthorisation());
        documentService.generateDefendantPinLetter(claim.getExternalId(), event.getPin(), event.getAuthorisation());

    }

    @EventListener
    public void uploadDocument(RepresentedClaimIssuedEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateSealedClaim(claim.getExternalId(), event.getAuthorisation());
    }

    @EventListener
    public void uploadDocument(DefendantResponseEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        if (!claim.getResponse().isPresent()) {
            throw new IllegalArgumentException("Response must be present");
        }
        documentService.generateDefendantResponseReceipt(claim.getExternalId(), event.getAuthorization());
    }

    @EventListener
    public void uploadDocument(CountyCourtJudgmentEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateCountyCourtJudgement(claim.getExternalId(), event.getAuthorisation());
    }

    @EventListener
    public void uploadDocument(AgreementCountersignedEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateSettlementAgreement(claim.getExternalId(), event.getAuthorisation());
    }

    @EventListener
    public void uploadDocument(CountersignSettlementAgreementEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateSettlementAgreement(claim.getExternalId(), event.getAuthorisation());
    }

}
