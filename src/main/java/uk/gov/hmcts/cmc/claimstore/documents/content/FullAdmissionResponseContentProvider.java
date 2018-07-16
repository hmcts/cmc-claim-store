package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.services.staff.content.RepaymentPlanContentProvider.create;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class FullAdmissionResponseContentProvider {

    private final StatementOfMeansContentProvider statementOfMeansContentProvider;

    public FullAdmissionResponseContentProvider(
        StatementOfMeansContentProvider statementOfMeansContentProvider
    ) {
        this.statementOfMeansContentProvider = statementOfMeansContentProvider;
    }

    public Map<String, Object> createContent(FullAdmissionResponse fullAdmissionResponse) {
        requireNonNull(fullAdmissionResponse);

        PaymentOption type = fullAdmissionResponse.getPaymentOption();

        ImmutableMap.Builder<String, Object> contentBuilder = new ImmutableMap.Builder<String, Object>()
            .put("responseTypeSelected", fullAdmissionResponse.getResponseType().getDescription())
            .put("paymentOption", type.getDescription())
            .put("whenWillTheyPay", createWhenTheyPay(fullAdmissionResponse));

        fullAdmissionResponse.getRepaymentPlan().ifPresent(repaymentPlan ->
            contentBuilder.put("repaymentPlan", create(type, repaymentPlan, repaymentPlan.getFirstPaymentDate()))
        );

        fullAdmissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> contentBuilder.putAll(
                statementOfMeansContentProvider.createContent(statementOfMeans)
            )
        );

        return contentBuilder.build();
    }

    private String createWhenTheyPay(FullAdmissionResponse fullAdmissionResponse) {
        switch (fullAdmissionResponse.getPaymentOption()) {
            case IMMEDIATELY:
            case BY_SPECIFIED_DATE:
                return "The full amount, no later than " + formatDate(fullAdmissionResponse.getPaymentDate()
                    .orElseThrow(IllegalStateException::new));
            default:
                return fullAdmissionResponse.getPaymentOption().getDescription();
        }
    }
}
