package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.CourtDeterminationAssert;
import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.ResponseAcceptationAssert;
import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.ResponseRejectionAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.DefendantEvidenceAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.DefendantTimelineAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.PaymentIntentionAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;


public class Assertions {

    private Assertions() {
    }

    public static PaymentIntentionAssert assertThat(PaymentIntention paymentIntention) {
        return new PaymentIntentionAssert(paymentIntention);
    }

    public static CourtDeterminationAssert assertThat(CourtDetermination courtDetermination) {
        return new CourtDeterminationAssert(courtDetermination);
    }

    public static ResponseRejectionAssert assertThat(ResponseRejection responseRejection) {
        return new ResponseRejectionAssert(responseRejection);
    }

    public static ResponseAcceptationAssert assertThat(ResponseAcceptation responseAcceptation) {
        return new ResponseAcceptationAssert(responseAcceptation);
    }

    public static AddressAssert assertThat(Address address) {
        return new AddressAssert(address);
    }

    public static CCDAddressAssert assertThat(CCDAddress ccdAddress) {
        return new CCDAddressAssert(ccdAddress);
    }

    public static TimelineEventAssert assertThat(TimelineEvent timelineEvent) {
        return new TimelineEventAssert(timelineEvent);
    }

    public static EvidenceRowAssert assertThat(EvidenceRow evidenceRow) {
        return new EvidenceRowAssert(evidenceRow);
    }

    public static DefendantTimelineAssert assertThat(DefendantTimeline timeline) {
        return new DefendantTimelineAssert(timeline);
    }

    public static DefendantEvidenceAssert assertThat(DefendantEvidence evidence) {
        return new DefendantEvidenceAssert(evidence);
    }

    public static ClaimAssert assertThat(Claim claim) {
        return new ClaimAssert(claim);
    }

    public static ClaimantAssert assertThat(Party party) {
        return new ClaimantAssert(party);
    }

    public static DefendantAssert assertThat(TheirDetails theirDetails) {
        return new DefendantAssert(theirDetails);
    }
}
