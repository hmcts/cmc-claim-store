package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.AddressMapper;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantPinLetterContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.ccd.util.PartyNameUtils.getPartyNameFor;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.claimstore.utils.Preconditions.requireNonBlank;

@Service
public class CitizenServiceDocumentsService {

    private final DocumentTemplates documentTemplates;
    private final ClaimContentProvider claimContentProvider;
    private final DefendantPinLetterContentProvider letterContentProvider;
    private final DocAssemblyService docAssemblyService;
    private final String defendantPinLetterTemplateID;
    private final CaseMapper caseMapper;
    private final NotificationsProperties notificationsProperties;
    private final StaffEmailProperties staffEmailProperties;
    private final InterestContentProvider interestContentProvider;
    private final AddressMapper addressMapper;

    @Autowired
    public CitizenServiceDocumentsService(
        DocumentTemplates documentTemplates,
        ClaimContentProvider claimContentProvider,
        DefendantPinLetterContentProvider letterContentProvider,
        DocAssemblyService docAssemblyService,
        @Value("${doc_assembly.defendantPinLetterTemplateID}") String defendantPinLetterTemplateID,
        CaseMapper caseMapper,
        NotificationsProperties notificationsProperties,
        StaffEmailProperties staffEmailProperties,
        InterestContentProvider interestContentProvider,
        AddressMapper addressMapper
    ) {
        this.documentTemplates = documentTemplates;
        this.claimContentProvider = claimContentProvider;
        this.letterContentProvider = letterContentProvider;
        this.docAssemblyService = docAssemblyService;
        this.defendantPinLetterTemplateID = defendantPinLetterTemplateID;
        this.caseMapper = caseMapper;
        this.notificationsProperties = notificationsProperties;
        this.staffEmailProperties = staffEmailProperties;
        this.interestContentProvider = interestContentProvider;
        this.addressMapper = addressMapper;
    }

    public Document sealedClaimDocument(Claim claim) {
        return new Document(new String(documentTemplates.getSealedClaim()), claimContentProvider.createContent(claim));
    }

    public Document draftClaimDocument(Claim claim) {
        return new Document(new String(documentTemplates.getDraftClaim()), claimContentProvider.createContent(claim));
    }

    public Document pinLetterDocument(Claim claim, String defendantPin) {
        return new Document(
            new String(documentTemplates.getDefendantPinLetter()),
            letterContentProvider.createContent(claim, defendantPin)
        );
    }

    public CCDDocument createDefendantPinLetter(Claim claim, String pin, String authorisation) {
        DocAssemblyTemplateBody formPayloadForPinLetter = pinLetterTemplateMapper(claim, pin);
        CCDCase ccdCase = caseMapper.to(claim);
        return docAssemblyService.generateDocument(ccdCase,
            authorisation,
            formPayloadForPinLetter,
            defendantPinLetterTemplateID);
    }

    public DocAssemblyTemplateBody pinLetterTemplateMapper(Claim claim, String defendantPin
    ) {
        requireNonNull(claim);
        requireNonBlank(defendantPin);

        List<BigDecimal> totalAmountComponents = new ArrayList<>();
        totalAmountComponents.add(((AmountBreakDown) claim.getClaimData()
            .getAmount())
            .getTotalAmount());
        totalAmountComponents.add(claim.getClaimData()
            .getFeesPaidInPounds().orElse(ZERO));

        if (!claim.getClaimData()
            .getInterest()
            .getType()
            .equals(Interest.InterestType.NO_INTEREST)) {
            InterestContent interestContent = interestContentProvider.createContent(
                claim.getClaimData()
                    .getInterest(),
                claim.getClaimData()
                    .getInterest()
                    .getInterestDate(),
                ((AmountBreakDown) claim.getClaimData()
                    .getAmount())
                    .getTotalAmount(),
                claim.getIssuedOn(),
                claim.getIssuedOn()
            );
            if (interestContent != null) {
                totalAmountComponents.add(interestContent.getAmountRealValue());
            }
        }

        return DocAssemblyTemplateBody.builder()
            .partyName(claim.getClaimData().getDefendant().getName())
            .claimAmount(formatMoney(
                totalAmountComponents.stream()
                    .filter(Objects::nonNull)
                    .reduce(ZERO, BigDecimal::add)))
            .referenceNumber(claim.getReferenceNumber())
            .partyAddress(addressMapper.to(claim.getClaimData().getDefendant().getAddress()))
            .claimantName(getPartyNameFor(claim.getClaimData().getClaimant()))
            .responseDeadline(claim.getResponseDeadline())
            .staffEmail(staffEmailProperties.getRecipient())
            .defendantPin(defendantPin)
            .respondToClaimUrl(notificationsProperties.getRespondToClaimUrl())
            .responseDashboardUrl(String.format("%s/response/dashboard",
                notificationsProperties.getFrontendBaseUrl()))
            .build();
    }
}
