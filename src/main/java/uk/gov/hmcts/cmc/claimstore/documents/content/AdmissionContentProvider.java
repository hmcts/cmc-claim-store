package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.services.staff.content.RepaymentPlanContentProvider.create;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class AdmissionContentProvider {
    private final StatementOfMeansContentProvider statementOfMeansContentProvider;

    public AdmissionContentProvider(
        StatementOfMeansContentProvider statementOfMeansContentProvider
    ) {
        this.statementOfMeansContentProvider = statementOfMeansContentProvider;
    }

    public Map<String, Object> createPaymentPlanDetails(
        PaymentOption paymentOption,
        RepaymentPlan repaymentPlan,
        LocalDate paymentDate,
        String paymentAmount
    ) {

        ImmutableMap.Builder<String, Object> contentBuilder = new ImmutableMap.Builder<String, Object>()
            .put("paymentOption", paymentOption.getDescription())
            .put("whenWillTheyPay", createWhenTheyPay(paymentOption, paymentDate, paymentAmount));

        Optional.ofNullable(repaymentPlan).ifPresent(plan ->
            contentBuilder.put("repaymentPlan", create(paymentOption, plan, plan.getFirstPaymentDate()))
        );

        return contentBuilder.build();
    }

    public Map<String, Object> createStatementOfMeansContent(StatementOfMeans statementOfMeans) {
        return statementOfMeansContentProvider.createContent(statementOfMeans);
    }


    private String createWhenTheyPay(PaymentOption paymentOption, LocalDate paymentDate, String paymentAmount) {
        switch (paymentOption) {
            case IMMEDIATELY:
            case FULL_BY_SPECIFIED_DATE:
                return paymentAmount
                    + ", no later than "
                    + formatDate(Optional.ofNullable(paymentDate)
                    .orElseThrow(IllegalStateException::new));
            default:
                return paymentOption.getDescription();
        }
    }
}
