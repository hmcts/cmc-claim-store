package uk.gov.hmcts.cmc.domain.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
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
    private final LocalDateTime settlementReachedAt;
    private final URI sealedClaimDocument;

    private CaseMetadata(
        Long id,
        String submitterId,
        String defendantId,
        String externalId,
        String referenceNumber,
        LocalDateTime createdAt,
        LocalDate issuedOn,
        LocalDate responseDeadline,
        LocalDateTime respondedAt, boolean moreTimeRequested,
        LocalDateTime countyCourtJudgmentRequestedAt,
        LocalDateTime settlementReachedAt,
        URI sealedClaimDocument
    ) {
        this.id = id;
        this.submitterId = submitterId;
        this.defendantId = defendantId;
        this.externalId = externalId;
        this.referenceNumber = referenceNumber;
        this.createdAt = createdAt;
        this.issuedOn = issuedOn;
        this.responseDeadline = responseDeadline;
        this.respondedAt = respondedAt;
        this.moreTimeRequested = moreTimeRequested;
        this.countyCourtJudgmentRequestedAt = countyCourtJudgmentRequestedAt;
        this.settlementReachedAt = settlementReachedAt;
        this.sealedClaimDocument = sealedClaimDocument;
    }

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
            claim.getSettlementReachedAt(),
            claim.getSealedClaimDocument().orElse(null)
        );
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
