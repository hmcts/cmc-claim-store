package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.domain.models.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.PartAdmissionResponse.PartAdmissionType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantPaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.EvidenceItem;
import uk.gov.hmcts.cmc.domain.models.response.HowMuchOwed;
import uk.gov.hmcts.cmc.domain.models.response.PayBySetDate;
import uk.gov.hmcts.cmc.domain.models.response.TimelineEvent;

public class SamplePartAdmissionResponse extends SampleResponse<SamplePartAdmissionResponse> {

    private PartAdmissionType partAdmissionType =
        PartAdmissionType.AMOUNT_TOO_HIGH;
    private HowMuchOwed howMuchOwed = SampleHowMuchOwed.validDefaults();
    private PayBySetDate payBySetDate = SamplePayBySetDate.validDefaults();
    private ImmutableList<EvidenceItem> evidence = ImmutableList.of(SampleEvidenceItem.validDefaults());
    private ImmutableList<TimelineEvent> timeline = ImmutableList.of(SampleTimelineEvent.validDefaults());
    private DefendantPaymentPlan defendantPaymentPlan = SampleDefendantPaymentPlan.validDefaults();
    private String impactOfDispute = "This dispute really upset me";

    public static SamplePartAdmissionResponse builder() {
        return new SamplePartAdmissionResponse();
    }

    public static PartAdmissionResponse validDefaults() {
        return builder().build();
    }

    public SamplePartAdmissionResponse withThatMuchOwed(HowMuchOwed owed) {
        this.howMuchOwed = owed;
        return this;
    }

    public SamplePartAdmissionResponse withPayBySetDate(PayBySetDate payBySetDate) {
        this.payBySetDate = payBySetDate;
        return this;
    }

    public SamplePartAdmissionResponse withEvidence(ImmutableList<EvidenceItem> evidence) {
        this.evidence = evidence;
        return this;
    }

    public SamplePartAdmissionResponse withTimeline(ImmutableList<TimelineEvent> timeline) {
        this.timeline = timeline;
        return this;
    }

    public SamplePartAdmissionResponse withDefendantPaymentPlan(DefendantPaymentPlan defendantPaymentPlan) {
        this.defendantPaymentPlan = defendantPaymentPlan;
        return this;
    }

    public SamplePartAdmissionResponse withImpactOfDispute(String impactOfDispute) {
        this.impactOfDispute = impactOfDispute;
        return this;
    }

    public PartAdmissionResponse build() {
        return new PartAdmissionResponse(
            freeMediationOption, moreTimeNeededOption, defendantDetails, statementOfTruth,
            partAdmissionType, howMuchOwed, payBySetDate, evidence, timeline,
            defendantPaymentPlan, impactOfDispute
        );
    }
}
