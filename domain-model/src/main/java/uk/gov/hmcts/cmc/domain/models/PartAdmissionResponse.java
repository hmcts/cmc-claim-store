package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.DefendantPaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.EvidenceItem;
import uk.gov.hmcts.cmc.domain.models.response.HowMuchOwed;
import uk.gov.hmcts.cmc.domain.models.response.TimelineEvent;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class PartAdmissionResponse extends Response {

    public enum PartAdmissionType {
        AMOUNT_TOO_HIGH,
        PAID_WHAT_BELIEVED_WAS_OWED
    }

    @Valid
    @NotNull
    private final ImmutableList<EvidenceItem> evidence;

    @Valid
    @NotNull
    private final ImmutableList<TimelineEvent> timeline;

    @NotNull
    private final PartAdmissionType partAdmissionType;

    @Valid
    @NotNull
    private final HowMuchOwed howMuchOwed;

    @Valid
    private final DefendantPaymentPlan defendantPaymentPlan;

    @NotBlank
    @Size(max = 99000)
    private final String impactOfDispute;

    public PartAdmissionResponse(
        FreeMediationOption freeMediation,
        MoreTimeNeededOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        ImmutableList<EvidenceItem> evidence,
        HowMuchOwed howMuchOwed,
        ImmutableList<TimelineEvent> timeline,
        DefendantPaymentPlan defendantPaymentPlan,
        String impactOfDispute,
        PartAdmissionType partAdmissionType) {
        super(freeMediation, moreTimeNeeded, defendant, statementOfTruth);
        this.evidence = evidence;
        this.howMuchOwed = howMuchOwed;
        this.timeline = timeline;
        this.defendantPaymentPlan = defendantPaymentPlan;
        this.impactOfDispute = impactOfDispute;
        this.partAdmissionType = partAdmissionType;
    }

    public ImmutableList<EvidenceItem> getEvidence() {
        return evidence;
    }

    public ImmutableList<TimelineEvent> getTimeline() {
        return timeline;
    }

    public PartAdmissionType getPartAdmissionType() {
        return partAdmissionType;
    }

    public DefendantPaymentPlan getDefendantPaymentPlan() {
        return defendantPaymentPlan;
    }

    public HowMuchOwed getHowMuchOwed() {
        return howMuchOwed;
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
            && Objects.equals(evidence, that.evidence)
            && Objects.equals(howMuchOwed, that.howMuchOwed)
            && Objects.equals(timeline, that.timeline)
            && Objects.equals(defendantPaymentPlan, that.defendantPaymentPlan)
            && Objects.equals(impactOfDispute, that.impactOfDispute)
            && Objects.equals(partAdmissionType, that.partAdmissionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), evidence, howMuchOwed, timeline,
            defendantPaymentPlan, impactOfDispute, partAdmissionType);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
