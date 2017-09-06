package uk.gov.hmcts.cmc.claimstore.events.utils.sampledata;

import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;

public final class SampleClaimIssuedEvent {

    private SampleClaimIssuedEvent() {
        // NO-OP
    }

    public static final String CLAIMANT_EMAIL = SampleClaim.SUBMITTER_EMAIL;
    public static final String REPRESENTATIVE_EMAIL = SampleClaim.SUBMITTER_EMAIL;
    public static final String DEFENDANT_EMAIL = SampleDefendantResponse.DEFENDANT_EMAIL;
    public static final String PIN = "Uyasd9834h";
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final DefendantResponse DEFENDANT_RESPONSE = SampleDefendantResponse.getDefault();

}
