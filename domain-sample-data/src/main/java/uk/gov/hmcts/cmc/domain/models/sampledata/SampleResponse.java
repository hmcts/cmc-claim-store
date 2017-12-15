package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.DefendantPaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.EvidenceItem;
import uk.gov.hmcts.cmc.domain.models.response.HowMuchOwed;
import uk.gov.hmcts.cmc.domain.models.response.TimelineEvent;
import java.util.List;

public abstract class SampleResponse<T extends SampleResponse<T>> {

    public static class PartAdmission extends SampleResponse<PartAdmission> {
        private PartAdmissionResponse.PartAdmissionType partAdmissionType = PartAdmissionResponse.PartAdmissionType.AMOUNT_TOO_HIGH;
        private String impactOfDispute = "Really upset me";

        public static PartAdmission builder() {
            return new PartAdmission();
        }

        public PartAdmission withPartAdmissionType(final PartAdmissionResponse.PartAdmissionType partAdmissionType) {
            this.partAdmissionType = partAdmissionType;
            return this;
        }

        public PartAdmission withPartAdmission(final Response.FreeMediationOption freeMediation, final Response.MoreTimeNeededOption moreTimeNeeded,
                                               final Party defendant, final StatementOfTruth statementOfTruth, final List<EvidenceItem> evidenceItems,
                                               final HowMuchOwed howMuchOwed, final List<TimelineEvent> timelineEvents, final DefendantPaymentPlan defendantPaymentPlan,
                                               final String impactOfDispute, final PartAdmissionResponse.PartAdmissionType partAdmissionType) {
            this.freeMediationOption = freeMediation;
            this.moreTimeNeededOption = moreTimeNeeded;
            this.defendantDetails = defendant;
            this.statementOfTruth = statementOfTruth;
            this.evidenceItems = evidenceItems;
            this.howMuchOwed = howMuchOwed;
            this.timelineEvents = timelineEvents;
            this.defendantPaymentPlan = defendantPaymentPlan;
            this.impactOfDispute = impactOfDispute;
            this.partAdmissionType = partAdmissionType;
            return this;
        }

        public PartAdmissionResponse build() {
            return new PartAdmissionResponse(
                freeMediationOption, moreTimeNeededOption, defendantDetails, statementOfTruth,
                evidenceItems, howMuchOwed, timelineEvents, defendantPaymentPlan, impactOfDispute, partAdmissionType
            );
        }
    }

    public static class FullDefence extends SampleResponse<FullDefence> {
        private FullDefenceResponse.DefenceType defenceType = FullDefenceResponse.DefenceType.DISPUTE;
        private String defence = "defence string";

        public static FullDefence builder() {
            return new FullDefence();
        }

        public FullDefence withDefenceType(final FullDefenceResponse.DefenceType defenceType) {
            this.defenceType = defenceType;
            return this;
        }

        public FullDefence withDefence(final String defence) {
            this.defence = defence;
            return this;
        }

        public FullDefenceResponse build() {
            return new FullDefenceResponse(
                freeMediationOption, moreTimeNeededOption, defendantDetails, statementOfTruth,
                defenceType, defence
            );
        }
    }

    protected List<EvidenceItem> evidenceItems = SampleEvidence.builder().build().getRows();
    protected HowMuchOwed howMuchOwed = SampleHowMuchOwed.builder().build();
    protected List<TimelineEvent> timelineEvents = SampleTimeLine.builder().build().getRows();
    protected DefendantPaymentPlan defendantPaymentPlan = SampleDefendantPaymentPlan.builder().build();
    protected PartAdmissionResponse.PartAdmissionType partAdmissionType = PartAdmissionResponse.PartAdmissionType.AMOUNT_TOO_HIGH;

    protected Response.FreeMediationOption freeMediationOption = Response.FreeMediationOption.YES;
    protected Response.MoreTimeNeededOption moreTimeNeededOption = Response.MoreTimeNeededOption.YES;
    protected Party defendantDetails = SampleParty.builder().withRepresentative(null).individual();
    protected StatementOfTruth statementOfTruth;

    public static FullDefenceResponse validDefaults() {
        return FullDefence.builder().build();
    }

    public static PartAdmissionResponse validPartAdmissionDefaults() {
        return PartAdmission.builder().build();
    }

    public T withMediation(final Response.FreeMediationOption freeMediationOption) {
        this.freeMediationOption = freeMediationOption;
        return (T)this;
    }

    public T withDefendantDetails(final Party sampleDefendantDetails) {
        this.defendantDetails = sampleDefendantDetails;
        return (T)this;
    }

    public T withStatementOfTruth(final String signerName, final String signerRole) {
        this.statementOfTruth = new StatementOfTruth(signerName,signerRole);
        return (T)this;
    }

    public abstract Response build();
}
