package uk.gov.hmcts.cmc.claimstore.controllers.support.orders;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;

import java.util.function.Supplier;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.claimstore.controllers.support.SupportController.CLAIM_DOES_NOT_EXIST;

@RestController
@RequestMapping("/support")
public class BulkPrintSupportController {
    private LegalOrderService legalOrderService;
    private final ClaimService claimService;
    private final UserService userService;
    private CaseMapper caseMapper;

    public BulkPrintSupportController(
        LegalOrderService legalOrderService,
        ClaimService claimService,
        UserService userService,
        CaseMapper caseMapper
    ) {
        this.legalOrderService = legalOrderService;
        this.claimService = claimService;
        this.userService = userService;
        this.caseMapper = caseMapper;
    }

    @PutMapping("/claim/{referenceNumber}/resend-order-for-print")
    @ApiOperation("Resend failed legal orders for bulk print")
    public void resendLegalAdvisorOrderToPrint(@PathVariable("referenceNumber") String referenceNumber) {
        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
            .orElseThrow(claimNotFoundException(referenceNumber));

        CCDCase ccdCase = caseMapper.to(claim);

        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();

        CCDDocument document = ccdCase.getCaseDocuments().stream()
            .map(CCDCollectionElement::getValue)
            .filter(d -> d.getDocumentType() == CCDClaimDocumentType.ORDER_DIRECTIONS)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("no order document found for the reference " + referenceNumber))
            .getDocumentLink();

        legalOrderService.print(authorisation, claim, document);
    }

    private CCDDocument to(ClaimDocument claimDocument) {
        final String documentUrl = claimDocument.getDocumentManagementUrl().toString();
        final String documentBinaryUrl = claimDocument.getDocumentManagementBinaryUrl().toString();
        return new CCDDocument(documentUrl, documentBinaryUrl, claimDocument.getDocumentName());
    }

    private Supplier<NotFoundException> claimNotFoundException(String reference) {
        return () -> new NotFoundException(format(CLAIM_DOES_NOT_EXIST, reference));
    }
}
