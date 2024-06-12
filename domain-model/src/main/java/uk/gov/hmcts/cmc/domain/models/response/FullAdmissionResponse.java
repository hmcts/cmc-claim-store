package uk.gov.hmcts.cmc.domain.models.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
public class FullAdmissionResponse extends Response {

    @NotNull
    @Valid
    private final PaymentIntention paymentIntention;

    @Valid
    private final StatementOfMeans statementOfMeans;

    @Builder(toBuilder = true)
    public FullAdmissionResponse(
        YesNoOption freeMediation,
        String mediationPhoneNumber,
        String mediationContactPerson,
        String noMediationReason,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        PaymentIntention paymentIntention,
        StatementOfMeans statementOfMeans,
        ResponseMethod responseMethod
    ) {
        super(
            FULL_ADMISSION,
            freeMediation,
            mediationPhoneNumber,
            mediationContactPerson,
            noMediationReason,
            moreTimeNeeded,
            defendant,
            statementOfTruth,
            responseMethod
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
