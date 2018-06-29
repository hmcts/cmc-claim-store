package uk.gov.hmcts.cmc.domain.models.response;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class PartAdmissionResponse extends Response {
    @NotNull
    private final DefenceType defenceType;

    @NotNull
    private final YesNoOption isAlreadyPaid;

    @Valid
    private final PaymentDetails paymentDetails;

    @Size(min = 1, max = 99000)
    private String defence;

    @Valid
    private final DefendantTimeline timeline;

    @Valid
    private final DefendantEvidence evidence;

    @Builder
    public PartAdmissionResponse(
        DefenceType defenceType,
        YesNoOption freeMediation,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        YesNoOption isAlreadyPaid,
        PaymentDetails paymentDetails,
        String defence,
        DefendantTimeline timeline,
        DefendantEvidence evidence
    ) {
        super(PART_ADMISSION, freeMediation, moreTimeNeeded, defendant, statementOfTruth);
        this.defenceType = defenceType;
        this.isAlreadyPaid = isAlreadyPaid;
        this.paymentDetails = paymentDetails;
        this.defence = defence;
        this.timeline = timeline;
        this.evidence = evidence;
    }

    public YesNoOption getIsAlreadyPaid() {
        return isAlreadyPaid;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
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


    public DefenceType getDefenceType() {
        return defenceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), defenceType, isAlreadyPaid, paymentDetails, defence, timeline, evidence);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
