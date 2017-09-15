package uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;

import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.RESPONSE_DEADLINE;

public final class SampleClaim {

    public static final Long USER_ID = 1L;
    public static final Long LETTER_HOLDER_ID = 2L;
    public static final Long DEFENDANT_ID = 4L;
    public static final Long CLAIM_ID = 3L;
    public static final String REFERENCE_NUMBER = "000CM001";
    public static final String EXTERNAL_ID = "external-id";
    public static final boolean NOT_REQUESTED_FOR_MORE_TIME = false;
    public static final String SUBMITTER_EMAIL = "claimant@mail.com";

    private SampleClaim() {
    }

    public static Claim getDefault() {
        return claim(null, REFERENCE_NUMBER);

    }

    public static Claim getDefaultForLegal() {
        return claim(SampleClaimData.validDefaults(), REFERENCE_NUMBER);
    }

    public static Claim claim(ClaimData claimData, String referenceNumber) {
        return new Claim(
            CLAIM_ID,
            USER_ID,
            LETTER_HOLDER_ID,
            DEFENDANT_ID,
            EXTERNAL_ID,
            referenceNumber,
            Optional.ofNullable(claimData).orElse(SampleClaimData.submittedByClaimant()),
            NOW_IN_LOCAL_ZONE,
            ISSUE_DATE,
            RESPONSE_DEADLINE,
            NOT_REQUESTED_FOR_MORE_TIME,
            SUBMITTER_EMAIL,
            null, response, defendantEmail);
    }

    public static Claim getWithSubmissionInterestDate() {
        return new Claim(
            CLAIM_ID,
            USER_ID,
            LETTER_HOLDER_ID,
            DEFENDANT_ID,
            EXTERNAL_ID,
            REFERENCE_NUMBER,
            SampleClaimData.builder().withInterestDate(SampleInterestDate.submission()).build(),
            NOW_IN_LOCAL_ZONE,
            ISSUE_DATE,
            RESPONSE_DEADLINE,
            NOT_REQUESTED_FOR_MORE_TIME,
            SUBMITTER_EMAIL,
            null, response, defendantEmail);
    }

    public static Claim getClaimWithNoDefendantEmail() {

        return new Claim(
            CLAIM_ID,
            USER_ID,
            LETTER_HOLDER_ID,
            DEFENDANT_ID,
            EXTERNAL_ID,
            REFERENCE_NUMBER,
            SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().withEmail(null).individualDetails())
                .build(),
            NOW_IN_LOCAL_ZONE,
            ISSUE_DATE,
            RESPONSE_DEADLINE,
            NOT_REQUESTED_FOR_MORE_TIME,
            SUBMITTER_EMAIL,
            null, response, defendantEmail);
    }
}
