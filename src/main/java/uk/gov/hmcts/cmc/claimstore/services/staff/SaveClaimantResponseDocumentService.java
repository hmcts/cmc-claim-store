package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
public class SaveClaimantResponseDocumentService {

    private final ClaimantResponseReceiptService claimantResponseReceiptService;
    private final DocumentsService documentService;
    private final UserService userService;

    @Autowired
    public SaveClaimantResponseDocumentService(
        ClaimantResponseReceiptService claimantResponseReceiptService,
        DocumentsService documentService,
        UserService userService
    ) {
        this.claimantResponseReceiptService = claimantResponseReceiptService;
        this.documentService = documentService;
        this.userService = userService;
    }

    public void getAndSaveDocumentToCcd(Claim claim) {
        PDF document = claimantResponseReceiptService.createPdf(claim);
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        documentService.uploadToDocumentManagement(document, authorisation, claim);
    }
}
