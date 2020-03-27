package uk.gov.hmcts.cmc.claimstore.controllers.support;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.util.ArrayList;
import java.util.Collections;
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
    private CaseMapper caseMapper;
    private DocumentManagementService documentManagementService;
    private Function<Claim, PDF> createDefendantResponsePdf;
    private Function<Claim, PDF> createSettlementPdf;
    private NotificationService notificationService;
    private UserService userService;
    private CoreCaseDataService coreCaseDataService;

    private static final String claimantNotificationEmailTemplateID = "594e64c9-edca-4dc5-bd83-35a44c4e28dc";
    private static final String defendantNotificationEmailTemplateID = "6d0a9f4d-f9e6-4962-b6c6-32c09169e6f2";
    private static final String REFERENCE_TEMPLATE = "Notify-Data-Fix-%s-%s";
    private static final String WORD_WILL = "will";

    private static final Predicate<ClaimDocument> filterDocsToRecreate = doc ->
        doc.getDocumentType().equals(ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT)
            || doc.getDocumentType().equals(ClaimDocumentType.SETTLEMENT_AGREEMENT);

    @Autowired
    public DataFixSupportController(SettlementAgreementCopyService settlementAgreementCopyService,
                                    DefendantResponseReceiptService defendantResponseReceiptService,
                                    ClaimService claimService,
                                    DocumentManagementService documentManagementService,
                                    NotificationService notificationService,
                                    UserService userService,
                                    CoreCaseDataService coreCaseDataService,
                                    CaseMapper caseMapper
    ) {

        this.claimService = claimService;
        this.caseMapper = caseMapper;
        this.documentManagementService = documentManagementService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.coreCaseDataService = coreCaseDataService;

        createDefendantResponsePdf = defendantResponseReceiptService::createPdf;
        createSettlementPdf = settlementAgreementCopyService::createPdf;
    }

    @PutMapping("/claim/{referenceNumber}/defendantName")
    @ApiOperation("Resend staff notifications associated with provided event")
    public void fixDefendantNameProvided(@PathVariable("referenceNumber") String referenceNumber) {

        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
            .orElseThrow(() -> new NotFoundException(referenceNumber));

        fixPartyStatements.andThen(fixClaimDocs)
            .andThen(updateClaim)
            .andThen(sendNotifications)
            .apply(claim);
    }

    private final UnaryOperator<Claim> fixPartyStatements = claim -> {

        if (claim.getSettlement().isEmpty()) {
            return claim;
        }

        Settlement newSettlement = new Settlement();
        claim.getSettlement().map(Settlement::getPartyStatements)
            .orElseGet(Collections::emptyList)
            .forEach(ps -> addPartyStatement(ps, newSettlement, claim.getClaimData().getDefendant().getName()));

        return claim.toBuilder().settlement(newSettlement).build();
    };

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
        return existinClaim.toBuilder().claimDocumentCollection(newCollection).build();
    }

    private UnaryOperator<Claim> updateClaim = claim -> {
        User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();
        coreCaseDataService.update(anonymousCaseWorker.getAuthorisation(),
            caseMapper.to(claim),
            CaseEvent.SUPPORT_UPDATE);
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

    private void addPartyStatement(PartyStatement partyStatement, Settlement settlement, String defendant) {
        if (partyStatement.getType() == StatementType.OFFER) {
            settlement.addOffer(
                fixOffer(partyStatement.getOffer().orElse(null), defendant),
                partyStatement.getMadeBy(),
                partyStatement.getId()
            );
        }

        if (partyStatement.getType() == StatementType.REJECTION) {
            settlement.addRejection(partyStatement.getMadeBy(), partyStatement.getId());
        }

        if (partyStatement.getType() == StatementType.ACCEPTATION) {
            settlement.addAcceptation(partyStatement.getMadeBy(), partyStatement.getId());
        }

        if (partyStatement.getType() == StatementType.COUNTERSIGNATURE) {
            settlement.addCounterSignature(partyStatement.getMadeBy(), partyStatement.getId());
        }
    }

    private Offer fixOffer(Offer offer, String defendantName) {
        return offer == null ? offer : Offer.builder()
            .content(fixOfferContent(offer.getContent(), defendantName))
            .completionDate(offer.getCompletionDate())
            .paymentIntention(offer.getPaymentIntention().orElse(null)).build();
    }

    private String fixOfferContent(String content, String defendantName) {
        return StringUtils.isEmpty(content) ? content : defendantName + " " + content.substring(content.indexOf(WORD_WILL));
    }

}
