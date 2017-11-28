package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Claim {

    private final Long id;
    private final String submitterId;
    private final String letterHolderId;
    private final String defendantId;
    private final String externalId;
    private final String referenceNumber;
    @JsonProperty("claim")
    private final ClaimData claimData;
    private final LocalDateTime createdAt;
    private final LocalDate issuedOn;
    private final LocalDate responseDeadline;
    private final boolean moreTimeRequested;
    private final String submitterEmail;
    private final LocalDateTime respondedAt;
    private final ResponseData response;
    private final String defendantEmail;
    private final CountyCourtJudgment countyCourtJudgment;
    private final LocalDateTime countyCourtJudgmentRequestedAt;
    private final Settlement settlement;
    private final LocalDateTime settlementReachedAt;

    @SuppressWarnings("squid:S00107") // Not sure there's a lot fo be done about removing parameters here
    public Claim(
        final Long id,
        final String submitterId,
        final String letterHolderId,
        final String defendantId,
        final String externalId,
        final String referenceNumber,
        final ClaimData claimData,
        final LocalDateTime createdAt,
        final LocalDate issuedOn,
        final LocalDate responseDeadline,
        final boolean moreTimeRequested,
        final String submitterEmail,

        final LocalDateTime respondedAt,
        final ResponseData response,
        final String defendantEmail,
        final CountyCourtJudgment countyCourtJudgment,
        final LocalDateTime countyCourtJudgmentRequestedAt,
        final Settlement settlement,
        final LocalDateTime settlementReachedAt
    ) {
        this.id = id;
        this.submitterId = submitterId;
        this.letterHolderId = letterHolderId;
        this.defendantId = defendantId;
        this.externalId = externalId;
        this.referenceNumber = referenceNumber;
        this.claimData = claimData;
        this.createdAt = createdAt;
        this.issuedOn = issuedOn;
        this.responseDeadline = responseDeadline;
        this.moreTimeRequested = moreTimeRequested;
        this.submitterEmail = submitterEmail;
        this.respondedAt = respondedAt;
        this.response = response;
        this.defendantEmail = defendantEmail;
        this.countyCourtJudgment = countyCourtJudgment;
        this.countyCourtJudgmentRequestedAt = countyCourtJudgmentRequestedAt;
        this.settlement = settlement;
        this.settlementReachedAt = settlementReachedAt;
    }

    public Long getId() {
        return id;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public String getLetterHolderId() {
        return letterHolderId;
    }

    public String getDefendantId() {
        return defendantId;
    }

    public String getExternalId() {
        return externalId;
    }

    public ClaimData getClaimData() {
        return claimData;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDate getIssuedOn() {
        return issuedOn;
    }

    public LocalDate getResponseDeadline() {
        return responseDeadline;
    }

    public boolean isMoreTimeRequested() {
        return moreTimeRequested;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public Optional<ResponseData> getResponse() {
        return Optional.ofNullable(response);
    }

    public String getDefendantEmail() {
        return defendantEmail;
    }

    public CountyCourtJudgment getCountyCourtJudgment() {
        return countyCourtJudgment;
    }

    public LocalDateTime getCountyCourtJudgmentRequestedAt() {
        return countyCourtJudgmentRequestedAt;
    }

    public Optional<Settlement> getSettlement() {
        return Optional.ofNullable(settlement);
    }

    public LocalDateTime getSettlementReachedAt() {
        return settlementReachedAt;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Claim claim = (Claim) obj;
        return moreTimeRequested == claim.moreTimeRequested
            && Objects.equals(id, claim.id)
            && Objects.equals(submitterId, claim.submitterId)
            && Objects.equals(letterHolderId, claim.letterHolderId)
            && Objects.equals(defendantId, claim.defendantId)
            && Objects.equals(externalId, claim.externalId)
            && Objects.equals(referenceNumber, claim.referenceNumber)
            && Objects.equals(claimData, claim.claimData)
            && Objects.equals(createdAt, claim.createdAt)
            && Objects.equals(issuedOn, claim.issuedOn)
            && Objects.equals(responseDeadline, claim.responseDeadline)
            && Objects.equals(submitterEmail, claim.submitterEmail)
            && Objects.equals(respondedAt, claim.respondedAt)
            && Objects.equals(response, claim.response)
            && Objects.equals(defendantEmail, claim.defendantEmail)
            && Objects.equals(countyCourtJudgment, claim.countyCourtJudgment)
            && Objects.equals(countyCourtJudgmentRequestedAt, claim.countyCourtJudgmentRequestedAt)
            && Objects.equals(settlement, claim.settlement)
            && Objects.equals(settlementReachedAt, claim.settlementReachedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, submitterId, letterHolderId, defendantId, externalId, referenceNumber,
            claimData, createdAt, issuedOn, responseDeadline, moreTimeRequested, submitterEmail,
            respondedAt, response, defendantEmail, countyCourtJudgment, countyCourtJudgmentRequestedAt,
            settlement, settlementReachedAt
        );
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
