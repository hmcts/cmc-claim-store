package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.AdmissionResponse;

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

    public Map<String, Object> createContent(AdmissionResponse admissionResponse) {
        requireNonNull(admissionResponse);

        PaymentOption type = admissionResponse.getPaymentOption();

        ImmutableMap.Builder<String, Object> contentBuilder = new ImmutableMap.Builder<String, Object>()
            .put("responseTypeSelected", admissionResponse.getResponseType().getDescription())
            .put("paymentOption", type.getDescription())
            .put("whenWillTheyPay", createWhenTheyPay(admissionResponse));

        admissionResponse.getRepaymentPlan().ifPresent(repaymentPlan ->
            contentBuilder.put("repaymentPlan", create(type, repaymentPlan, repaymentPlan.getFirstPaymentDate()))
        );

        admissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> contentBuilder.putAll(
                statementOfMeansContentProvider.createContent(statementOfMeans)
            )
        );

        return contentBuilder.build();
    }

    private String createWhenTheyPay(AdmissionResponse admissionResponse) {
        switch (admissionResponse.getPaymentOption()) {
            case IMMEDIATELY:
            case FULL_BY_SPECIFIED_DATE:
                return "The full amount, no later than " + formatDate(admissionResponse.getPaymentDate()
                    .orElseThrow(IllegalStateException::new));
            default:
                return admissionResponse.getPaymentOption().getDescription();
        }
    }
}
