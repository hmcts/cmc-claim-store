package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

// Create these fields in JSON when serialize Java object, ignore them when deserialize.
@JsonIgnoreProperties(
    value = {"totalAmountTillToday", "totalAmountTillDateOfIssue",
        "amountWithInterestUntilIssueDate", "totalInterestTillDateOfIssue", "totalInterest",
        "serviceDate", "amountWithInterest", "directionsQuestionnaireDeadline", "claimSubmissionOperationIndicators"},
    allowGetters = true
)
@Getter
@EqualsAndHashCode
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
    private LocalDate serviceDate;
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
    private final List<String> features;
    private final LocalDateTime claimantRespondedAt;
    private final ClaimantResponse claimantResponse;
    private final LocalDate directionsQuestionnaireDeadline;
    @DateNotInTheFuture
    private final LocalDate moneyReceivedOn;
    private final ReDetermination reDetermination;
    private final LocalDateTime reDeterminationRequestedAt;
    private final ClaimDocumentCollection claimDocumentCollection;
    private final LocalDate claimantResponseDeadline;
    private final ClaimState state;
    private final ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators;
    private final Long ccdCaseId;
    private final ReviewOrder reviewOrder;

    @SuppressWarnings("squid:S00107") // Not sure there's a lot fo be done about removing parameters here
    @Builder(toBuilder = true)
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
        LocalDate serviceDate,
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
        List<String> features,
        LocalDateTime claimantRespondedAt,
        ClaimantResponse claimantResponse,
        LocalDate directionsQuestionnaireDeadline,
        LocalDate moneyReceivedOn,
        ReDetermination reDetermination,
        LocalDateTime reDeterminationRequestedAt,
        ClaimDocumentCollection claimDocumentCollection,
        LocalDate claimantResponseDeadline,
        ClaimState state,
        ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators,
        Long ccdCaseId,
        ReviewOrder reviewOrder
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
        this.serviceDate = serviceDate;
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
        this.features = features;
        this.claimantRespondedAt = claimantRespondedAt;
        this.claimantResponse = claimantResponse;
        this.directionsQuestionnaireDeadline = directionsQuestionnaireDeadline;
        this.moneyReceivedOn = moneyReceivedOn;
        this.reDetermination = reDetermination;
        this.reDeterminationRequestedAt = reDeterminationRequestedAt;
        this.claimDocumentCollection = claimDocumentCollection;
        this.claimantResponseDeadline = claimantResponseDeadline;
        this.state = state;
        this.ccdCaseId = ccdCaseId;
        this.claimSubmissionOperationIndicators = claimSubmissionOperationIndicators;
        this.reviewOrder = reviewOrder;
    }

    public Optional<Response> getResponse() {
        return Optional.ofNullable(response);
    }

    public Optional<Settlement> getSettlement() {
        return Optional.ofNullable(settlement);
    }

    @JsonIgnore
    public Optional<URI> getClaimDocument(ClaimDocumentType claimDocumentType) {
        if (claimDocumentCollection == null) {
            return Optional.empty();
        } else {
            Optional<ClaimDocument> claimDocument = claimDocumentCollection.getDocument(claimDocumentType);
            if (claimDocument.isPresent()) {
                return Optional.ofNullable(claimDocument.get().getDocumentManagementUrl());
            }
        }
        return Optional.empty();
    }

    public LocalDate getServiceDate() {
        return serviceDate == null ? issuedOn.plusDays(5) : serviceDate;
    }

    public Optional<BigDecimal> getAmountWithInterest() {
        return TotalAmountCalculator.amountWithInterest(this);
    }

    public Optional<BigDecimal> getAmountWithInterestUntilIssueDate() {
        return TotalAmountCalculator.amountWithInterestUntilIssueDate(this);
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

    public Optional<BigDecimal> getTotalInterestTillDateOfIssue() {
        return TotalAmountCalculator.calculateInterestForClaim(this, issuedOn);
    }

    public Optional<ClaimantResponse> getClaimantResponse() {
        return Optional.ofNullable(claimantResponse);
    }

    public Optional<LocalDateTime> getClaimantRespondedAt() {
        return Optional.ofNullable(claimantRespondedAt);
    }

    public Optional<LocalDate> getMoneyReceivedOn() {
        return Optional.ofNullable(moneyReceivedOn);
    }

    public Optional<LocalDateTime> getReDeterminationRequestedAt() {
        return Optional.ofNullable(reDeterminationRequestedAt);
    }

    public Optional<ReDetermination> getReDetermination() {
        return Optional.ofNullable(reDetermination);
    }

    public Optional<ClaimDocumentCollection> getClaimDocumentCollection() {
        return Optional.ofNullable(claimDocumentCollection);
    }

    public Optional<LocalDate> getClaimantResponseDeadline() {
        return Optional.ofNullable(claimantResponseDeadline);
    }

    @JsonIgnore
    public Optional<ClaimState> getState() {
        return Optional.ofNullable(state);
    }

    public Optional<ReviewOrder> getReviewOrder() {
        return Optional.ofNullable(reviewOrder);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
