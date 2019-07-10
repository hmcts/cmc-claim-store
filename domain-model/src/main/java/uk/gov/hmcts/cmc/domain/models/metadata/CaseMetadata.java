package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
public class CaseMetadata {
    private final Long id;
    private final String submitterId;
    private final String submitterPartyType;
    private final String defendantId;
    private final String defendantPartyType;
    private final String externalId;
    private final String referenceNumber;
    private final LocalDateTime createdAt;
    private final LocalDate issuedOn;
    private final LocalDate responseDeadline;
    private final Boolean moreTimeRequested;
    private final CountyCourtJudgmentMetadata countyCourtJudgment;
    private final DefendantResponseMetadata defendantResponse;
    private final ClaimantResponseMetadata claimantResponse;
    private final SettlementMetadata settlement;
    private final RedeterminationMetadata redetermination;
    private final URI sealedClaimDocument;
    private final String paymentReference;
    private final LocalDate moneyReceivedOn;
    private final ClaimState state;
    private final ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators;

    public static CaseMetadata fromClaim(Claim claim) {
        return new CaseMetadata(
            claim.getId(),
            claim.getSubmitterId(),
            claim.getClaimData().getClaimant().getClass().getSimpleName(),
            claim.getDefendantId(),
            claim.getClaimData().getDefendant().getClass().getSimpleName(),
            claim.getExternalId(),
            claim.getReferenceNumber(),
            claim.getCreatedAt(),
            claim.getIssuedOn(),
            claim.getResponseDeadline(),
            claim.isMoreTimeRequested(),
            CountyCourtJudgmentMetadata.fromClaim(claim),
            DefendantResponseMetadata.fromClaim(claim),
            ClaimantResponseMetadata.fromClaim(claim),
            SettlementMetadata.fromClaim(claim),
            RedeterminationMetadata.fromClaim(claim),
            claim.getClaimDocument(SEALED_CLAIM)
                .map(ClaimDocument::getDocumentManagementUrl)
                .orElse(null),
            Optional.ofNullable(claim.getClaimData().getPayment())
                .map(Payment::getReference)
                .orElse(null),
            claim.getMoneyReceivedOn().orElse(null),
            claim.getState().orElse(null),
            claim.getClaimSubmissionOperationIndicators()
        );
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
