package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CaseMetadata {
    private final Long id;
    private final String externalId;
    private final String referenceNumber;
    private final LocalDateTime createdAt;
    private final LocalDate issuedOn;
    private final String paymentReference;
    private final List<String> features;
    private final URI sealedClaimDocument;
    private final String submitterId;
    private final List<String> submitterPartyTypes;
    private final String defendantId;
    private final List<String> defendantPartyTypes;
    private final LocalDate responseDeadline;
    private final Boolean moreTimeRequested;
    private final DefendantResponseMetadata defendantResponse;
    private final LocalDate claimantResponseDeadline;
    private final ClaimantResponseMetadata claimantResponse;
    private final LocalDate directionsQuestionnaireDeadline;
    private final LocalDate intentionToProceedDeadline;
    private final SettlementMetadata settlement;
    private final CountyCourtJudgmentMetadata countyCourtJudgment;
    private final RedeterminationMetadata redetermination;
    private final LocalDate moneyReceivedOn;
    private final ClaimState state;
    private final ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators;

    public static CaseMetadata fromClaim(Claim claim) {
        return CaseMetadata.builder()
            .id(claim.getId())
            .externalId(claim.getExternalId())
            .referenceNumber(claim.getReferenceNumber())
            .createdAt(claim.getCreatedAt())
            .issuedOn(claim.getIssuedOn())
            .paymentReference(claim.getClaimData().getPayment()
                .map(Payment::getReference)
                .orElse(null))
            .features(claim.getFeatures())
            .sealedClaimDocument(claim.getClaimDocument(SEALED_CLAIM)
                .map(ClaimDocument::getDocumentManagementUrl)
                .orElse(null))
            .submitterId(claim.getSubmitterId())
            .submitterPartyTypes(claim.getClaimData().getClaimants().stream()
                .map(Party::getClass).map(Class::getSimpleName)
                .collect(toList()))
            .defendantId(claim.getDefendantId())
            .defendantPartyTypes(claim.getClaimData().getDefendants().stream()
                .map(TheirDetails::getClass).map(Class::getSimpleName)
                .collect(toList()))
            .responseDeadline(claim.getResponseDeadline())
            .moreTimeRequested(claim.isMoreTimeRequested())
            .defendantResponse(DefendantResponseMetadata.fromClaim(claim))
            .claimantResponseDeadline(claim.getClaimantResponseDeadline().orElse(null))
            .claimantResponse(ClaimantResponseMetadata.fromClaim(claim))
            .directionsQuestionnaireDeadline(claim.getDirectionsQuestionnaireDeadline())
            .intentionToProceedDeadline(claim.getIntentionToProceedDeadline())
            .settlement(SettlementMetadata.fromClaim(claim))
            .countyCourtJudgment(CountyCourtJudgmentMetadata.fromClaim(claim))
            .redetermination(RedeterminationMetadata.fromClaim(claim))
            .moneyReceivedOn(claim.getMoneyReceivedOn().orElse(null))
            .state(claim.getState())
            .claimSubmissionOperationIndicators(claim.getClaimSubmissionOperationIndicators())
            .build();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
