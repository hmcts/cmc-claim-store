package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private final List<EvidenceItem> evidenceItems;

    @Valid
    @NotNull
    private final List<TimelineEvent> timelineEvents;

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

    public PartAdmissionType getPartialAdmissionType() {
        return partAdmissionType;
    }


    public PartAdmissionResponse(
        final FreeMediationOption freeMediation,
        final MoreTimeNeededOption moreTimeNeeded,
        final Party defendant,
        final StatementOfTruth statementOfTruth,
        final List<EvidenceItem> evidenceItems,
        final HowMuchOwed howMuchOwed,
        final List<TimelineEvent> timelineEvents,
        final DefendantPaymentPlan defendantPaymentPlan,
        final String impactOfDispute,
        final PartAdmissionType partAdmissionType)
    {
        super(freeMediation, moreTimeNeeded, defendant, statementOfTruth);
        this.evidenceItems = evidenceItems;
        this.howMuchOwed = howMuchOwed;
        this.timelineEvents = timelineEvents;
        this.defendantPaymentPlan = defendantPaymentPlan;
        this.impactOfDispute = impactOfDispute;
        this.partAdmissionType = partAdmissionType;
    }

    public String getImpactOfDispute() {
        return impactOfDispute;
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
            && Objects.equals(evidenceItems, that.evidenceItems)
            && Objects.equals(howMuchOwed, that.howMuchOwed)
            && Objects.equals(timelineEvents, that.timelineEvents)
            && Objects.equals(partAdmissionType, that.partAdmissionType)
            && Objects.equals(defendantPaymentPlan, that.defendantPaymentPlan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evidenceItems, howMuchOwed, timelineEvents, defendantPaymentPlan, partAdmissionType);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
