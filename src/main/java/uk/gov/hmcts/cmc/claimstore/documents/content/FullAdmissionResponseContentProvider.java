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
        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();
        RepaymentPlan repaymentPlan = null;
        PaymentOption type = fullAdmissionResponse.getPaymentOption();
        Optional<RepaymentPlan> optionalRepaymentPlan = fullAdmissionResponse.getRepaymentPlan();
        if (optionalRepaymentPlan.isPresent()) {
            repaymentPlan = optionalRepaymentPlan.get();
            contentBuilder.put("repaymentPlan", create(type, repaymentPlan, repaymentPlan.getFirstPaymentDate()));
        }
        contentBuilder.put("responseTypeSelected", fullAdmissionResponse.getResponseType().getDescription());
        contentBuilder.put("paymentOption", type.getDescription());

        fullAdmissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> contentBuilder.putAll(
                statementOfMeansContentProvider.createContent(statementOfMeans)
            )
        );

        return contentBuilder.build();
    }
}
