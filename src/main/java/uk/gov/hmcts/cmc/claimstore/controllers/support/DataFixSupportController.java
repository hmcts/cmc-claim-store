package uk.gov.hmcts.cmc.claimstore.controllers.support;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

@RestController
@RequestMapping("/support")
public class DataFixSupportController {

    private final ClaimService claimService;
    private DocumentManagementService documentManagementService;
    private Function<Claim, PDF> createDefendantResponsePdf;
    private Function<Claim, PDF> createSettlementPdf;
    private NotificationService notificationService;
    private UserService userService;

    private static final String claimantNotificationEmailTemplateID = "594e64c9-edca-4dc5-bd83-35a44c4e28dc";
    private static final String defendantNotificationEmailTemplateID = "6d0a9f4d-f9e6-4962-b6c6-32c09169e6f2";
    private static final String REFERENCE_TEMPLATE = "Notify-Data-Fix-%s-%s";

    private static final Predicate<ClaimDocument> filterDocsToRecreate = doc ->
        doc.getDocumentType().equals(ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT) ||
            doc.getDocumentType().equals(ClaimDocumentType.SETTLEMENT_AGREEMENT) ||
            doc.getDocumentType().equals(ClaimDocumentType.ORDER_DIRECTIONS);

    @Autowired
    public DataFixSupportController(SettlementAgreementCopyService settlementAgreementCopyService,
                                    DefendantResponseReceiptService defendantResponseReceiptService,
                                    ClaimService claimService,
                                    DocumentManagementService documentManagementService,
                                    NotificationService notificationService,
                                    UserService userService
    ) {

        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.notificationService = notificationService;
        this.userService = userService;

        createDefendantResponsePdf = defendantResponseReceiptService::createPdf;
        createSettlementPdf = settlementAgreementCopyService::createPdf;
    }

    @PutMapping("/claim/{referenceNumber}/defendantName")
    @ApiOperation("Resend staff notifications associated with provided event")
    public void fixDefendantNameProvided(@PathVariable("referenceNumber") String referenceNumber) {

        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
            .orElseThrow(() -> new NotFoundException(referenceNumber));

        checkIfDefendantNameNeedsChange
            .andThen(fixDefendantName)
            .andThen(fixClaimDocs)
            .andThen(sendNotifications)
            .apply(claim);

    }

    private Function<Claim, List<ClaimDocument>> getClaimDocs = claim -> claim.getClaimDocumentCollection()
        .map(ClaimDocumentCollection::getClaimDocuments)
        .orElseThrow(IllegalAccessError::new);


    private final UnaryOperator<Claim> fixClaimDocs = claim -> {

        List<ClaimDocument> docsRegenerated = new ArrayList<>();

        List<ClaimDocument> docsToRecreate = getClaimDocs.apply(claim)
            .stream()
            .filter(filterDocsToRecreate)
            .collect(Collectors.toList());

        User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();
        PDF newlyCreatedDocument = null;
        for (ClaimDocument doc : docsToRecreate) {

            if (doc.getDocumentType().equals(DEFENDANT_RESPONSE_RECEIPT)) {
                newlyCreatedDocument = createDefendantResponsePdf.apply(claim);
            } else if (doc.getDocumentType().equals(SETTLEMENT_AGREEMENT)) {
                newlyCreatedDocument = createSettlementPdf.apply(claim);
            }
            docsRegenerated.add(documentManagementService.uploadDocument(anonymousCaseWorker.getAuthorisation(),
                newlyCreatedDocument));
        }
        return updateClaimDocCollection(docsRegenerated, claim);

    };

    private Claim updateClaimDocCollection(List<ClaimDocument> existingList, Claim existinClaim) {
        ClaimDocumentCollection docCollection = existinClaim.getClaimDocumentCollection()
            .orElseThrow(IllegalArgumentException::new);
        ClaimDocumentCollection newCollection = new ClaimDocumentCollection();

        docCollection.getStaffUploadedDocuments().forEach(newCollection::addStaffUploadedDocument);
        docCollection.getScannedDocuments().forEach(newCollection::addScannedDocument);
        docCollection.getClaimDocuments().stream().filter(filterDocsToRecreate.negate())
            .forEach(newCollection::addClaimDocument);
        existingList.forEach(newCollection::addClaimDocument);
        existinClaim.toBuilder().claimDocumentCollection(newCollection);
        return existinClaim;
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

    private Function<Claim, Claim> checkIfDefendantNameNeedsChange = claim -> {
        if (!claim.getResponse().isPresent()) {
            throw new IllegalStateException("No response found");
        }

        if (getDefendantName.apply(claim).equals(getClaimantProvidedDefendantName.apply(claim))) {
            throw new IllegalStateException("Defendant provided name is no different from one provided by Claimant.");
        }

        return claim;
    };

    private UnaryOperator<Claim> sendNotifications = claim -> {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            claimantNotificationEmailTemplateID,
            prepareNotificationParameters(claim),
            String.format(REFERENCE_TEMPLATE, "Claimant", claim.getReferenceNumber())
        );

        notificationService.sendMail(
            claim.getDefendantEmail(),
            defendantNotificationEmailTemplateID,
            prepareNotificationParameters(claim),
            String.format(REFERENCE_TEMPLATE, "Defendant", claim.getReferenceNumber())
        );

        return claim;
    };

    private Map<String, String> prepareNotificationParameters(Claim claim) {
        return ImmutableMap.of(
            "claimReferenceNumber", claim.getReferenceNumber(),
            "claimantName", claim.getClaimData().getClaimant().getName(),
            "defendantName", claim.getClaimData().getDefendant().getName());
    }

}
