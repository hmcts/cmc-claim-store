package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class PartAdmissionResponse extends AdmissionResponse {
    @NotNull
    @JsonUnwrapped
    private final YesNoOption isAlreadyPaid;

    @Valid
    @NotNull
    private final HowMuchHaveYouPaid howMuchHaveYouPaid;

    @Size(min = 1, max = 99000)
    private String defence;

    @Valid
    private final DefendantTimeline timeline;

    @Valid
    private final DefendantEvidence evidence;

    @Builder
    public PartAdmissionResponse(
        YesNoOption freeMediation,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        PaymentOption paymentOption,
        LocalDate paymentDate,
        RepaymentPlan repaymentPlan,
        StatementOfMeans statementOfMeans,
        YesNoOption isAlreadyPaid,
        HowMuchHaveYouPaid howMuchHaveYouPaid,
        String defence,
        DefendantTimeline timeline,
        DefendantEvidence evidence
    ) {
        super(PART_ADMISSION, freeMediation, moreTimeNeeded, defendant, statementOfTruth,
            paymentOption, paymentDate, repaymentPlan, statementOfMeans);
        this.isAlreadyPaid = isAlreadyPaid;
        this.howMuchHaveYouPaid = howMuchHaveYouPaid;
        this.defence = defence;
        this.timeline = timeline;
        this.evidence = evidence;
    }

    public YesNoOption getIsAlreadyPaid() {
        return isAlreadyPaid;
    }

    public HowMuchHaveYouPaid getHowMuchHaveYouPaid() {
        return howMuchHaveYouPaid;
    }

    public String getDefence() {
        return defence;
    }

    public Optional<DefendantTimeline> getTimeline() {
        return Optional.ofNullable(timeline);
    }

    public Optional<DefendantEvidence> getEvidence() {
        return Optional.ofNullable(evidence);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        PartAdmissionResponse that = (PartAdmissionResponse) other;
        return super.equals(other)
            && isAlreadyPaid == that.isAlreadyPaid
            && Objects.equals(howMuchHaveYouPaid, that.howMuchHaveYouPaid)
            && Objects.equals(defence, that.defence)
            && Objects.equals(timeline, that.timeline)
            && Objects.equals(evidence, that.evidence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isAlreadyPaid, howMuchHaveYouPaid, defence, timeline, evidence);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
