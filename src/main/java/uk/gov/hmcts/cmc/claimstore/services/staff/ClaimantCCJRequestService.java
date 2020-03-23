package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CCJByAdmissionOrDeterminationPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.DETERMINATION;

@Service
public class ClaimantCCJRequestService {

    private final CCJByAdmissionOrDeterminationPdfService ccjByAdmissionOrDeterminationPdfService;
    private final DocumentsService documentService;
    private final boolean ctscEnabled;
    private final ClaimService claimService;

    @Autowired
    public ClaimantCCJRequestService(
        CCJByAdmissionOrDeterminationPdfService ccjByAdmissionOrDeterminationPdfService,
        DocumentsService documentService,
        @Value("${feature_toggles.ctsc_enabled}") boolean ctscEnabled,
        ClaimService claimService
    ) {
        this.ccjByAdmissionOrDeterminationPdfService = ccjByAdmissionOrDeterminationPdfService;
        this.documentService = documentService;
        this.ctscEnabled = ctscEnabled;
        this.claimService = claimService;
    }

    @EventListener
    public void uploadDocumentToDocumentStore(CountyCourtJudgmentEvent event) {
        Claim claim = event.getClaim();
        if (claim.getCountyCourtJudgment().getCcjType() == ADMISSIONS
            || claim.getCountyCourtJudgment().getCcjType() == DETERMINATION) {
            if (ctscEnabled) {
                claimService.updateClaimState(event.getAuthorisation(), claim, ClaimState.JUDGMENT_REQUESTED);
                PDF document = ccjByAdmissionOrDeterminationPdfService.createPdf(claim);
                documentService.uploadToDocumentManagement(document, event.getAuthorisation(), claim);
            } else {
                claimService.updateClaimState(event.getAuthorisation(), claim, ClaimState.OPEN);
            }
        }
    }
}
