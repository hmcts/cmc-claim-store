package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.DefendantPaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.EvidenceItem;
import uk.gov.hmcts.cmc.domain.models.response.HowMuchOwed;
import uk.gov.hmcts.cmc.domain.models.response.TimelineEvent;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PartAdmissionResponse extends Response {

    public enum PartAdmissionType {
        AMOUNT_TOO_HIGH,
        PAID_WHAT_BELIEVED_WAS_OWED
    }

    @NotNull
    private final PartAdmissionType partAdmissionType;

    @Valid
    @NotNull
    private final ImmutableList<EvidenceItem> evidence;

    @Valid
    @NotNull
    private final ImmutableList<TimelineEvent> timeline;

    @Valid
    @NotNull
    private final HowMuchOwed howMuchOwed;

    @Valid
    private final DefendantPaymentPlan defendantPaymentPlan;

    @Size(max = 99000)
    private final String impactOfDispute;

    public PartAdmissionResponse(
        FreeMediationOption freeMediation,
        MoreTimeNeededOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        PartAdmissionType partAdmissionType,
        HowMuchOwed howMuchOwed,
        ImmutableList<EvidenceItem> evidence,
        ImmutableList<TimelineEvent> timeline,
        DefendantPaymentPlan defendantPaymentPlan,
        String impactOfDispute
    ) {
        super(freeMediation, moreTimeNeeded, defendant, statementOfTruth);
        this.partAdmissionType = partAdmissionType;
        this.howMuchOwed = howMuchOwed;
        this.evidence = evidence;
        this.timeline = timeline;
        this.defendantPaymentPlan = defendantPaymentPlan;
        this.impactOfDispute = impactOfDispute;
    }

    public PartAdmissionType getPartAdmissionType() {
        return partAdmissionType;
    }

    public HowMuchOwed getHowMuchOwed() {
        return howMuchOwed;
    }

    public ImmutableList<EvidenceItem> getEvidence() {
        return evidence;
    }

    public ImmutableList<TimelineEvent> getTimeline() {
        return timeline;
    }

    public DefendantPaymentPlan getDefendantPaymentPlan() {
        return defendantPaymentPlan;
    }

    public String getImpactOfDispute() {
        return impactOfDispute;
    }

    public PartAdmissionType getPartialAdmissionType() {
        return partAdmissionType;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final PartAdmissionResponse that = (PartAdmissionResponse) other;
        return super.equals(that)
            && Objects.equals(partAdmissionType, that.partAdmissionType)
            && Objects.equals(howMuchOwed, that.howMuchOwed)
            && Objects.equals(evidence, that.evidence)
            && Objects.equals(timeline, that.timeline)
            && Objects.equals(defendantPaymentPlan, that.defendantPaymentPlan)
            && Objects.equals(impactOfDispute, that.impactOfDispute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
            partAdmissionType,
            howMuchOwed,
            evidence,
            timeline,
            defendantPaymentPlan,
            impactOfDispute
        );
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
