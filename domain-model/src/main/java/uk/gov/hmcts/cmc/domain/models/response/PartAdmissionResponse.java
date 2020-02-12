package uk.gov.hmcts.cmc.domain.models.response;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.constraints.ValidAdmission;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@ValidAdmission
@EqualsAndHashCode(callSuper = true)
public class PartAdmissionResponse extends Response {

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal amount;

    @Valid
    private final PaymentDeclaration paymentDeclaration;

    @Valid
    private final PaymentIntention paymentIntention;

    @NotNull
    @Size(min = 1, max = 99000)
    private final String defence;

    @Valid
    private final DefendantTimeline timeline;

    @Valid
    private final DefendantEvidence evidence;

    @Valid
    private final StatementOfMeans statementOfMeans;

    @Valid
    private final DirectionsQuestionnaire directionsQuestionnaire;

    @SuppressWarnings("squid:S00107") // Not sure there's a lot of be done about removing parameters here
    @Builder
    public PartAdmissionResponse(
        YesNoOption freeMediation,
        String mediationPhoneNumber,
        String mediationContactPerson,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        BigDecimal amount,
        PaymentDeclaration paymentDeclaration,
        PaymentIntention paymentIntention,
        String defence,
        DefendantTimeline timeline,
        DefendantEvidence evidence,
        StatementOfMeans statementOfMeans,
        DirectionsQuestionnaire directionsQuestionnaire
    ) {
        super(
            PART_ADMISSION,
            freeMediation,
            mediationPhoneNumber,
            mediationContactPerson,
            moreTimeNeeded,
            defendant,
            statementOfTruth
        );
        this.amount = amount;
        this.paymentDeclaration = paymentDeclaration;
        this.paymentIntention = paymentIntention;
        this.defence = defence;
        this.timeline = timeline;
        this.evidence = evidence;
        this.statementOfMeans = statementOfMeans;
        this.directionsQuestionnaire = directionsQuestionnaire;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Optional<PaymentDeclaration> getPaymentDeclaration() {
        return Optional.ofNullable(paymentDeclaration);
    }

    public Optional<PaymentIntention> getPaymentIntention() {
        return Optional.ofNullable(paymentIntention);
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

    public Optional<StatementOfMeans> getStatementOfMeans() {
        return Optional.ofNullable(statementOfMeans);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

    public Optional<DirectionsQuestionnaire> getDirectionsQuestionnaire() {
        return Optional.ofNullable(directionsQuestionnaire);
    }
}
