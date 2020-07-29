package uk.gov.hmcts.cmc.claimstore.events.utils.sampledata;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;

public final class SampleClaimIssuedEvent {

    public static final String CLAIMANT_EMAIL = SampleClaim.SUBMITTER_EMAIL;
    public static final String SUBMITTER_SURNAME = "Smith";
    public static final String SUBMITTER_FORENAME = "Steven";
    public static final String DEFENDANT_EMAIL = SampleClaim.DEFENDANT_EMAIL;
    public static final String PIN = "Uyasd9834h";
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final Claim CLAIM_LEGAL_REP = SampleClaim.getLegalDataWithReps();
    public static final Claim CLAIM_NO_RESPONSE = SampleClaim.builder().build();
    public static final Claim CLAIM_WITH_RESPONSE = SampleClaim.getWithDefaultResponse();
    public static final Claim CLAIM_WITH_DEFAULT_CCJ = SampleClaim
        .builder()
        .withCountyCourtJudgment(SampleCountyCourtJudgment
            .builder()
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build())
        .build();
    public static final Claim CLAIM_WITH_CCJ_BY_ADMISSION = SampleClaim
        .builder()
        .withCountyCourtJudgment(SampleCountyCourtJudgment
            .builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .build())
        .build();

    private SampleClaimIssuedEvent() {
        // NO-OP
    }

}
