package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/support/fix")
public class DataFixSupportController {


    private final SettlementAgreementCopyService settlementPdfService;
    private final DefendantResponseReceiptService defendantPdfService;
    private final DocumentManagementService documentManagementService;
    private final ClaimService claimService;

    private static final Predicate<ClaimDocument> filterDocsToRecreate = doc ->
        doc.getDocumentType().equals(ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT) ||
            doc.getDocumentType().equals(ClaimDocumentType.SETTLEMENT_AGREEMENT) ||
            doc.getDocumentType().equals(ClaimDocumentType.ORDER_DIRECTIONS);

    @Autowired
    public DataFixSupportController(SettlementAgreementCopyService settlementPdfService,
                                    DefendantResponseReceiptService defendantPdfService,
                                    ClaimService claimService,
                                    DocumentManagementService documentManagementService) {
        this.documentManagementService = documentManagementService;
        this.settlementPdfService = settlementPdfService;
        this.defendantPdfService = defendantPdfService;
        this.claimService = claimService;
    }

    @PutMapping("/defendantName/claim/{referenceNumber}/")
    @ApiOperation("Resend staff notifications associated with provided event")
    public void fixDefendantNameProvided(
        @PathVariable("referenceNumber") String referenceNumber
    ) {

        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
            .orElseThrow(() -> new NotFoundException(referenceNumber));

        fixDefendantName
            .andThen(checkIfDefendantNameNeedsChange)
            .andThen()
            .apply(claim);

    }

    private Function<Claim, List<ClaimDocument>> getClaimDocs = claim -> claim.getClaimDocumentCollection()
        .map(ClaimDocumentCollection::getClaimDocuments)
        .orElseThrow(IllegalAccessError::new);

    private final Function<Claim, Claim> fixClaimDocs = claim -> {

        List<ClaimDocument> docsToFix = getClaimDocs.apply(claim).stream().filter(filterDocsToRecreate)
            .collect(Collectors.toList());

        ClaimDocumentCollection claimDocCollection = claim.getClaimDocumentCollection()
            .orElse(new ClaimDocumentCollection());

        for(ClaimDocument doc : docsToFix){

            doc.getDocumentType()
        }

    }

    // TODO - Looks like we may not need to fix the defendant name.
    private Function<Claim, Claim> fixDefendantName = claim -> claim.toBuilder().build();



    private final Function<Claim, String> getDefendantName = claim -> claim.getResponse()
        .map(Response::getDefendant)
        .map(Party::getName)
        .orElse("UNKNOWN");

    private final Function<Claim, String> getClaimantProvidedDefendantName = claim -> claim.getResponse()
        .map(Response::getDefendant)
        .map(Party::getName)
        .orElse("NOTPROVIDED");

    private  Function<Claim, Claim> checkIfDefendantNameNeedsChange = claim ->{
        if(!claim.getResponse().isPresent()){
            throw new IllegalStateException("No response found");
        }

        if(!getDefendantName.apply(claim).equalsIgnoreCase(getClaimantProvidedDefendantName.apply(claim))){
            throw new IllegalStateException("Not valid for this endpoint");
        }

        return claim;
    };



    public void thingstodo() {

        PDF document = settlementPdfService.createPdf(claim);
        PDF defendantPdf = defendantPdfService.createPdf(claim);

    }

}
