package uk.gov.hmcts.cmc.claimstore.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Claim {

    private final Long id;
    private final Long submitterId;
    private final Long letterHolderId;
    private final Long defendantId;
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
    private final Map<String, Object> countyCourtJudgment;
    private final LocalDateTime countyCourtJudgmentRequestedAt;

    @SuppressWarnings("squid:S00107") // Not sure there's a lot fo be done about remove parameters here
    public Claim(
        final Long id,
        final Long submitterId,
        final Long letterHolderId,
        final Long defendantId,
        final String externalId,
        final String referenceNumber,
        final ClaimData claimData,
        final LocalDateTime createdAt,
        final LocalDate issuedOn,
        final LocalDate responseDeadline,
        final boolean moreTimeRequested,
        final String submitterEmail,
        final LocalDateTime respondedAt,
        Map<String, Object> countyCourtJudgment,
        LocalDateTime countyCourtJudgmentRequestedAt) {
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
        this.countyCourtJudgment = countyCourtJudgment;
        this.countyCourtJudgmentRequestedAt = countyCourtJudgmentRequestedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSubmitterId() {
        return submitterId;
    }

    public Long getLetterHolderId() {
        return letterHolderId;
    }

    public Long getDefendantId() {
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

    public Map<String, Object> getCountyCourtJudgment() {
        return countyCourtJudgment;
    }

    public LocalDateTime getCountyCourtJudgmentRequestedAt() {
        return countyCourtJudgmentRequestedAt;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Claim otherClaim = (Claim) other;
        return Objects.equals(id, otherClaim.id)
            && Objects.equals(submitterId, otherClaim.submitterId)
            && Objects.equals(submitterEmail, otherClaim.submitterEmail)
            && Objects.equals(letterHolderId, otherClaim.letterHolderId)
            && Objects.equals(defendantId, otherClaim.defendantId)
            && Objects.equals(externalId, otherClaim.externalId)
            && Objects.equals(referenceNumber, otherClaim.referenceNumber)
            && Objects.equals(claimData, otherClaim.claimData)
            && Objects.equals(createdAt, otherClaim.createdAt)
            && Objects.equals(issuedOn, otherClaim.issuedOn)
            && Objects.equals(responseDeadline, otherClaim.responseDeadline)
            && Objects.equals(respondedAt, otherClaim.respondedAt)
            && Objects.equals(countyCourtJudgment, otherClaim.countyCourtJudgment)
            && Objects.equals(countyCourtJudgmentRequestedAt, otherClaim.countyCourtJudgmentRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id, submitterId, submitterEmail, letterHolderId, defendantId, externalId, referenceNumber, claimData,
            createdAt, issuedOn, responseDeadline, moreTimeRequested, respondedAt
        );
    }

    public static class Builder {
        private Long id;
        private String externalId;
        private Long submitterId;
        private String submitterEmail;
        private Long letterHolderId;
        private Long defendantId;
        private String referenceNumber;
        private ClaimData claimData;
        private LocalDate issuedOn;
        private LocalDateTime createdAt;
        private LocalDate responseDeadline;
        private boolean moreTimeRequested;
        private LocalDateTime respondedAt;
        private Map<String, Object> countyCourtJudgment;
        private LocalDateTime countyCourtJudgmentRequestedAt;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder setSubmitterId(Long submitterId) {
            this.submitterId = submitterId;
            return this;
        }

        public Builder setSubmitterEmail(String submitterEmail) {
            this.submitterEmail = submitterEmail;
            return this;
        }

        public Builder setLetterHolderId(Long letterHolderId) {
            this.letterHolderId = letterHolderId;
            return this;
        }

        public Builder setDefendantId(Long defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder setReferenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
            return this;
        }

        public Builder setClaimData(ClaimData claimData) {
            this.claimData = claimData;
            return this;
        }

        public Builder setIssuedOn(LocalDate issuedOn) {
            this.issuedOn = issuedOn;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setResponseDeadline(LocalDate responseDeadline) {
            this.responseDeadline = responseDeadline;
            return this;
        }

        public Builder setMoreTimeRequested(boolean moreTimeRequested) {
            this.moreTimeRequested = moreTimeRequested;
            return this;
        }

        public Builder setRespondedAt(LocalDateTime respondedAt) {
            this.respondedAt = respondedAt;
            return this;
        }

        public Builder setCountyCourtJudgment(Map<String, Object> countyCourtJudgment) {
            this.countyCourtJudgment = countyCourtJudgment;
            return this;
        }

        public Builder setCountyCourtJudgmentRequestedAt(LocalDateTime countyCourtJudgmentRequestedAt) {
            this.countyCourtJudgmentRequestedAt = countyCourtJudgmentRequestedAt;
            return this;
        }

        public Claim build() {
            return new Claim(
                id, submitterId, letterHolderId, defendantId, externalId, referenceNumber,
                claimData, createdAt, issuedOn, responseDeadline, moreTimeRequested,
                submitterEmail, respondedAt, countyCourtJudgment, countyCourtJudgmentRequestedAt
            );
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
