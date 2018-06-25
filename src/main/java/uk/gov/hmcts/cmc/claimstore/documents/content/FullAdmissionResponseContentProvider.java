package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.services.staff.content.RepaymentPlanContentProvider.create;

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
            .put("paymentOption", type.getDescription());

        Optional<RepaymentPlan> optionalRepaymentPlan = fullAdmissionResponse.getRepaymentPlan();
        if (optionalRepaymentPlan.isPresent()) {
            RepaymentPlan repaymentPlan = optionalRepaymentPlan.get();
            contentBuilder.put("repaymentPlan", create(type, repaymentPlan, repaymentPlan.getFirstPaymentDate()));
        }

        fullAdmissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> contentBuilder.putAll(
                statementOfMeansContentProvider.createContent(statementOfMeans)
            )
        );

        return contentBuilder.build();
    }
}
