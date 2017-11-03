package uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.models.offers.Settlement;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.RESPONSE_DEADLINE;

public final class SampleClaim {

    public static final String USER_ID = "1";
    public static final String LETTER_HOLDER_ID = "2";
    public static final String DEFENDANT_ID = "4";
    public static final Long CLAIM_ID = 3L;
    public static final String REFERENCE_NUMBER = "000CM001";
    public static final String EXTERNAL_ID = UUID.randomUUID().toString();
    public static final String DOCUMENT_MANAGEMENT_ID = UUID.randomUUID().toString();
    public static final boolean NOT_REQUESTED_FOR_MORE_TIME = false;
    public static final LocalDateTime NOT_RESPONDED = null;
    public static final String SUBMITTER_EMAIL = "claimant@mail.com";
    public static final String DEFENDANT_EMAIL = SampleTheirDetails.DEFENDANT_EMAIL;

    private String submitterId = USER_ID;
    private String letterHolderId = LETTER_HOLDER_ID;
    private String defendantId = DEFENDANT_ID;
    private Long claimId = CLAIM_ID;
    private String referenceNumber = REFERENCE_NUMBER;
    private String externalId = EXTERNAL_ID;
    private String documentManagementId = DOCUMENT_MANAGEMENT_ID;
    private boolean isMoreTimeRequested = NOT_REQUESTED_FOR_MORE_TIME;
    private LocalDate responseDeadline = RESPONSE_DEADLINE;
    private String submitterEmail = SUBMITTER_EMAIL;
    private LocalDateTime createdAt = NOW_IN_LOCAL_ZONE;
    private LocalDateTime respondedAt = NOT_RESPONDED;
    private LocalDate issuedOn = ISSUE_DATE;
    private CountyCourtJudgment countyCourtJudgment = null;
    private LocalDateTime countyCourtJudgmentRequestedAt = null;
    private ClaimData claimData = SampleClaimData.validDefaults();
    private ResponseData response;
    private String defendantEmail;
    private Settlement settlement = null;
    private LocalDateTime settlementReachedAt = null;

    private SampleClaim() {
    }

    public static Claim getDefault() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .withPaymentOptionImmediately()
                    .build()
            ).build();
    }

    public static Claim getWithDefaultResponse() {
        return getWithResponse(SampleResponseData.validDefaults());
    }

    public static Claim getWithResponse(final ResponseData responseData) {
        return builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withResponse(responseData)
            .withDefendantEmail(DEFENDANT_EMAIL)
            .build();
    }

    public static Claim getDefaultForLegal() {
        return builder().build();
    }

    public static Claim claim(ClaimData claimData, String referenceNumber) {
        return new Claim(
            CLAIM_ID,
            USER_ID,
            LETTER_HOLDER_ID,
            DEFENDANT_ID,
            EXTERNAL_ID,
            DOCUMENT_MANAGEMENT_ID,
            referenceNumber,
            Optional.ofNullable(claimData).orElse(SampleClaimData.submittedByClaimant()),
            NOW_IN_LOCAL_ZONE,
            ISSUE_DATE,
            RESPONSE_DEADLINE,
            NOT_REQUESTED_FOR_MORE_TIME,
            SUBMITTER_EMAIL,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static Claim getWithSubmissionInterestDate() {
        return builder()
            .withClaimData(
                SampleClaimData.builder().withInterestDate(SampleInterestDate.submission()).build()
            )
            .build();
    }

    public static Claim getWithResponseDeadline(LocalDate responseDeadline) {
        return builder().withResponseDeadline(responseDeadline).build();
    }

    public static Claim getClaimWithNoDefendantEmail() {

        return SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withDefendant(SampleTheirDetails.builder().withEmail(null).individualDetails())
                    .build()
            ).build();
    }

    public static SampleClaim builder() {
        return new SampleClaim();
    }

    public Claim build() {
        return new Claim(
            claimId,
            submitterId,
            letterHolderId,
            defendantId,
            externalId,
            documentManagementId,
            referenceNumber,
            claimData,
            NOW_IN_LOCAL_ZONE,
            issuedOn,
            responseDeadline,
            isMoreTimeRequested,
            submitterEmail,
            respondedAt,
            response,
            defendantEmail,
            countyCourtJudgment,
            countyCourtJudgmentRequestedAt,
            settlement,
            settlementReachedAt);
    }

    public SampleClaim withSubmitterId(String userId) {
        this.submitterId = userId;
        return this;
    }

    public SampleClaim withLetterHolderId(String letterHolderId) {
        this.letterHolderId = letterHolderId;
        return this;
    }

    public SampleClaim withDefendantId(String defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public SampleClaim withClaimId(Long claimId) {
        this.claimId = claimId;
        return this;
    }

    public SampleClaim withReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
        return this;
    }

    public SampleClaim withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public SampleClaim withMoreTimeRequested(boolean moreTimeRequested) {
        isMoreTimeRequested = moreTimeRequested;
        return this;
    }

    public SampleClaim withResponseDeadline(LocalDate responseDeadline) {
        this.responseDeadline = responseDeadline;
        return this;
    }

    public SampleClaim withSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
        return this;
    }

    public SampleClaim withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public SampleClaim withIssuedOn(LocalDate issuedOn) {
        this.issuedOn = issuedOn;
        return this;
    }

    public SampleClaim withCountyCourtJudgment(CountyCourtJudgment countyCourtJudgment) {
        this.countyCourtJudgment = countyCourtJudgment;
        return this;
    }

    public SampleClaim withCountyCourtJudgmentRequestedAt(LocalDateTime countyCourtJudgmentRequestedAt) {
        this.countyCourtJudgmentRequestedAt = countyCourtJudgmentRequestedAt;
        return this;
    }

    public SampleClaim withClaimData(ClaimData claimData) {
        this.claimData = claimData;
        return this;
    }

    public SampleClaim withRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
        return this;
    }

    public SampleClaim withResponse(final ResponseData responseData) {
        this.response = responseData;
        return this;
    }

    public SampleClaim withDefendantEmail(final String defendantEmail) {
        this.defendantEmail = defendantEmail;
        return this;
    }
}
