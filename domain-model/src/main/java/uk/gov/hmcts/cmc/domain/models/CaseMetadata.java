package uk.gov.hmcts.cmc.domain.models;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

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
    private final String defendantId;
    private final String externalId;
    private final String referenceNumber;
    private final LocalDateTime createdAt;
    private final LocalDate issuedOn;
    private final LocalDate responseDeadline;
    private final LocalDateTime respondedAt;
    private final boolean moreTimeRequested;
    private final LocalDateTime countyCourtJudgmentRequestedAt;
    private final LocalDateTime claimantRespondedAt;
    private final LocalDateTime settlementReachedAt;
    private final URI sealedClaimDocument;
    private final String paymentReference;

    public static CaseMetadata fromClaim(Claim claim) {
        return new CaseMetadata(
            claim.getId(),
            claim.getSubmitterId(),
            claim.getDefendantId(),
            claim.getExternalId(),
            claim.getReferenceNumber(),
            claim.getCreatedAt(),
            claim.getIssuedOn(),
            claim.getResponseDeadline(),
            claim.getRespondedAt(),
            claim.isMoreTimeRequested(),
            claim.getCountyCourtJudgmentRequestedAt(),
            claim.getClaimantRespondedAt().orElse(null),
            claim.getSettlementReachedAt(),
            claim.getClaimDocument(SEALED_CLAIM).orElse(null),
            Optional.ofNullable(claim.getClaimData().getPayment())
                .map(Payment::getReference)
                .orElse(null)
        );
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
