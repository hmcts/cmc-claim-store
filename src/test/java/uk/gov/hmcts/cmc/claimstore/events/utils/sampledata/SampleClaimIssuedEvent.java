package uk.gov.hmcts.cmc.claimstore.events.utils.sampledata;

import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaim;

public final class SampleClaimIssuedEvent {

    private SampleClaimIssuedEvent() {
        // NO-OP
    }

    public static final String CLAIMANT_EMAIL = SampleClaim.SUBMITTER_EMAIL;
    public static final String SUBMITTER_SURNAME = "Smith";
    public static final String SUBMITTER_FORENAME = "Steven";
    private static final String SUBMITTER_NAME = SampleClaimIssuedEvent.SUBMITTER_FORENAME
        + " " + SampleClaimIssuedEvent.SUBMITTER_SURNAME;
    public static final String REPRESENTATIVE_EMAIL = SampleClaim.SUBMITTER_EMAIL;
    public static final String DEFENDANT_EMAIL = SampleClaim.DEFENDANT_EMAIL;
    public static final String PIN = "Uyasd9834h";
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final Claim CLAIM_WITH_RESPONSE = SampleClaim.getWithDefaultResponse();

}
