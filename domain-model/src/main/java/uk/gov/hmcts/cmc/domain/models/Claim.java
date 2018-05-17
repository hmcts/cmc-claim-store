package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

// Create these fields in JSON when serialize Java object, ignore them when deserialize.
@JsonIgnoreProperties(
    value = {"totalAmountTillToday", "totalAmountTillDateOfIssue", "totalInterest", "serviceDate"},
    allowGetters = true
)
@Builder
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
    private final Response response;
    private final String defendantEmail;
    private final CountyCourtJudgment countyCourtJudgment;
    private final LocalDateTime countyCourtJudgmentRequestedAt;
    private final Settlement settlement;
    private final LocalDateTime settlementReachedAt;
    private final String sealedClaimDocumentSelfPath;

    @SuppressWarnings("squid:S00107") // Not sure there's a lot fo be done about removing parameters here
    @JsonCreator
    public Claim(
        Long id,
        String submitterId,
        String letterHolderId,
        String defendantId,
        String externalId,
        String referenceNumber,
        ClaimData claimData,
        LocalDateTime createdAt,
        LocalDate issuedOn,
        LocalDate responseDeadline,
        boolean moreTimeRequested,
        String submitterEmail,
        LocalDateTime respondedAt,
        Response response,
        String defendantEmail,
        CountyCourtJudgment countyCourtJudgment,
        LocalDateTime countyCourtJudgmentRequestedAt,
        Settlement settlement,
        LocalDateTime settlementReachedAt,
        String sealedClaimDocumentSelfPath
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
        this.sealedClaimDocumentSelfPath = sealedClaimDocumentSelfPath;
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

    public Optional<Response> getResponse() {
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

    public Optional<String> getSealedClaimDocumentSelfPath() {
        return Optional.ofNullable(sealedClaimDocumentSelfPath);
    }

    public LocalDate getServiceDate() {
        return issuedOn.plusDays(5);
    }

    public Optional<BigDecimal> getTotalAmountTillToday() {
        return TotalAmountCalculator.totalTillToday(this);
    }

    public Optional<BigDecimal> getTotalAmountTillDateOfIssue() {
        return TotalAmountCalculator.totalTillDateOfIssue(this);
    }

    public Optional<BigDecimal> getTotalInterest() {
        return TotalAmountCalculator.calculateInterestForClaim(this);
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
            && Objects.equals(settlementReachedAt, claim.settlementReachedAt)
            && Objects.equals(sealedClaimDocumentSelfPath, claim.sealedClaimDocumentSelfPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, submitterId, letterHolderId, defendantId, externalId, referenceNumber,
            claimData, createdAt, issuedOn, responseDeadline, moreTimeRequested, submitterEmail,
            respondedAt, response, defendantEmail, countyCourtJudgment, countyCourtJudgmentRequestedAt,
            settlement, settlementReachedAt, sealedClaimDocumentSelfPath
        );
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
