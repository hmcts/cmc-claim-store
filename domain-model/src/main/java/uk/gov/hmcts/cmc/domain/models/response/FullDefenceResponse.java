package uk.gov.hmcts.cmc.domain.models.response;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
public class FullDefenceResponse extends Response {

    @NotNull
    private final DefenceType defenceType;

    @Size(min = 1, max = 99000)
    private final String defence;

    @Valid
    private final PaymentDeclaration paymentDeclaration;

    @Valid
    private final DefendantTimeline timeline;

    @Valid
    private final DefendantEvidence evidence;

    public FullDefenceResponse(
        YesNoOption freeMediation,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        DefenceType defenceType,
        String defence,
        PaymentDeclaration paymentDeclaration,
        DefendantTimeline timeline,
        DefendantEvidence evidence
    ) {
        super(FULL_DEFENCE, freeMediation, moreTimeNeeded, defendant, statementOfTruth);
        this.defenceType = defenceType;
        this.defence = defence;
        this.paymentDeclaration = paymentDeclaration;
        this.timeline = timeline;
        this.evidence = evidence;
    }

    public DefenceType getDefenceType() {
        return defenceType;
    }

    public Optional<String> getDefence() {
        return Optional.ofNullable(defence);
    }

    public Optional<PaymentDeclaration> getPaymentDeclaration() {
        return Optional.ofNullable(paymentDeclaration);
    }

    public Optional<DefendantTimeline> getTimeline() {
        return Optional.ofNullable(timeline);
    }

    public Optional<DefendantEvidence> getEvidence() {
        return Optional.ofNullable(evidence);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
