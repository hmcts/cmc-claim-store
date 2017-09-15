package uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.RESPONSE_DEADLINE;

public final class SampleClaim {

    public static final Long USER_ID = 1L;
    public static final Long LETTER_HOLDER_ID = 2L;
    public static final Long DEFENDANT_ID = 4L;
    public static final Long CLAIM_ID = 3L;
    public static final String REFERENCE_NUMBER = "000CM001";
    public static final String EXTERNAL_ID = UUID.randomUUID().toString();
    public static final boolean NOT_REQUESTED_FOR_MORE_TIME = false;
    public static final LocalDateTime NOT_RESPONDED = null;
    public static final String SUBMITTER_EMAIL = "claimant@mail.com";

    private Long submitterId = USER_ID;
    private Long letterHolderId = LETTER_HOLDER_ID;
    private Long defendantId = DEFENDANT_ID;
    private Long claimId = CLAIM_ID;
    private String referenceNumber = REFERENCE_NUMBER;
    private String externalId = EXTERNAL_ID;
    private boolean isMoreTimeRequested = NOT_REQUESTED_FOR_MORE_TIME;
    private LocalDate responseDeadline = RESPONSE_DEADLINE;
    private String submitterEmail = SUBMITTER_EMAIL;
    private LocalDateTime createdAt = NOW_IN_LOCAL_ZONE;
    private LocalDateTime respondedAt = NOT_RESPONDED;
    private LocalDate issuedOn = ISSUE_DATE;
    private Map<String, Object> countyCourtJudgment = null;
    private LocalDateTime countyCourtJudgmentRequestedAt = null;
    private ClaimData claimData = SampleClaimData.validDefaults();

    private SampleClaim() {
    }

    public static Claim getDefault() {
        return builder().withClaimData(SampleClaimData.submittedByClaimant()).build();
    }

    public static Claim getDefaultForLegal() {
        return builder().build();
    }

    public static Claim getWithSubmissionInterestDate() {
        return builder()
            .withClaimData(
                SampleClaimData.builder().withInterestDate(SampleInterestDate.submission()).build()
            ).build();
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
            referenceNumber,
            claimData,
            createdAt,
            issuedOn,
            responseDeadline,
            isMoreTimeRequested,
            submitterEmail,
            respondedAt,
            countyCourtJudgment,
            countyCourtJudgmentRequestedAt
        );
    }

    public SampleClaim withUserId(Long userId) {
        this.submitterId = userId;
        return this;
    }

    public SampleClaim withLetterHolderId(Long letterHolderId) {
        this.letterHolderId = letterHolderId;
        return this;
    }

    public SampleClaim withDefendantId(Long defendantId) {
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

    public SampleClaim withCountyCourtJudgment(Map<String, Object> countyCourtJudgment) {
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
}
