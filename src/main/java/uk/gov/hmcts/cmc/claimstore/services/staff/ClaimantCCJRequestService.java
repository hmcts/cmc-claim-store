package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CCJByAdmissionOrDeterminationPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.DETERMINATION;

@Service
public class ClaimantCCJRequestService {

    private final CCJByAdmissionOrDeterminationPdfService ccjByAdmissionOrDeterminationPdfService;
    private final DocumentsService documentService;

    @Autowired
    public ClaimantCCJRequestService(
        CCJByAdmissionOrDeterminationPdfService ccjByAdmissionOrDeterminationPdfService,
        DocumentsService documentService
    ) {
        this.ccjByAdmissionOrDeterminationPdfService = ccjByAdmissionOrDeterminationPdfService;
        this.documentService = documentService;
    }

    @EventListener
    public void getAndSaveDocumentToCcd(CountyCourtJudgmentEvent event) {
        Claim claim = event.getClaim();
        if (claim.getCountyCourtJudgment().getCcjType() == ADMISSIONS
            || claim.getCountyCourtJudgment().getCcjType() == DETERMINATION) {
            PDF document = ccjByAdmissionOrDeterminationPdfService.createPdf(claim);
            documentService.uploadToDocumentManagement(document, event.getAuthorisation(), claim);
        }
    }
}
