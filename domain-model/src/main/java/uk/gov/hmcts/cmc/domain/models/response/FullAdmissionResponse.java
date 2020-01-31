package uk.gov.hmcts.cmc.domain.models.response;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
public class FullAdmissionResponse extends Response {

    @NotNull
    @Valid
    private final PaymentIntention paymentIntention;

    @Valid
    private final StatementOfMeans statementOfMeans;

    @Builder
    public FullAdmissionResponse(
        YesNoOption freeMediation,
        String mediationPhoneNumber,
        String mediationContactPerson,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        PaymentIntention paymentIntention,
        StatementOfMeans statementOfMeans
    ) {
        super(
            FULL_ADMISSION,
            freeMediation,
            mediationPhoneNumber,
            mediationContactPerson,
            moreTimeNeeded,
            defendant,
            statementOfTruth
        );
        this.paymentIntention = paymentIntention;
        this.statementOfMeans = statementOfMeans;
    }

    public PaymentIntention getPaymentIntention() {
        return paymentIntention;
    }

    public Optional<StatementOfMeans> getStatementOfMeans() {
        return Optional.ofNullable(statementOfMeans);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
