package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.Payment;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

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
    private final URI sealedClaimDocument;
    private final String submitterId;
    private final String submitterPartyType;
    private final String defendantId;
    private final String defendantPartyType;
    private final LocalDate responseDeadline;
    private final Boolean moreTimeRequested;
    private final DefendantResponseMetadata defendantResponse;
    private final ClaimantResponseMetadata claimantResponse;
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
            .paymentReference(Optional.ofNullable(claim.getClaimData().getPayment())
                .map(Payment::getReference)
                .orElse(null))
            .sealedClaimDocument(claim.getClaimDocument(SEALED_CLAIM)
                .map(ClaimDocument::getDocumentManagementUrl)
                .orElse(null))
            .submitterId(claim.getSubmitterId())
            .submitterPartyType(claim.getClaimData().getClaimant().getClass().getSimpleName())
            .defendantId(claim.getDefendantId())
            .defendantPartyType(claim.getClaimData().getDefendant().getClass().getSimpleName())
            .responseDeadline(claim.getResponseDeadline())
            .moreTimeRequested(claim.isMoreTimeRequested())
            .defendantResponse(DefendantResponseMetadata.fromClaim(claim))
            .claimantResponse(ClaimantResponseMetadata.fromClaim(claim))
            .settlement(SettlementMetadata.fromClaim(claim))
            .countyCourtJudgment(CountyCourtJudgmentMetadata.fromClaim(claim))
            .redetermination(RedeterminationMetadata.fromClaim(claim))
            .moneyReceivedOn(claim.getMoneyReceivedOn().orElse(null))
            .state(claim.getState().orElse(null))
            .claimSubmissionOperationIndicators(claim.getClaimSubmissionOperationIndicators())
            .build();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
