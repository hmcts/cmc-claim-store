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

        RepaymentPlan repaymentPlan = null;
        PaymentOption type = fullAdmissionResponse.getPaymentOption();
        Optional<RepaymentPlan> optionalRepaymentPlan = fullAdmissionResponse.getRepaymentPlan();
        if (optionalRepaymentPlan.isPresent()) {
            repaymentPlan = optionalRepaymentPlan.get();
        }
        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();
        contentBuilder.put("responseTypeSelected", fullAdmissionResponse.getResponseType().getDescription());

        switch (type) {
            case IMMEDIATELY:
                contentBuilder.put("paymentOption", type.getDescription());
                break;
            case FULL_BY_SPECIFIED_DATE:
                contentBuilder.put("paymentOption", type.getDescription());
                fullAdmissionResponse.getStatementOfMeans().ifPresent(
                    statementOfMeans -> contentBuilder.putAll(
                        statementOfMeansContentProvider.createContent(statementOfMeans)
                    )
                );
                break;
            case INSTALMENTS:
                contentBuilder.put("paymentOption", type.getDescription());
                contentBuilder.put("repaymentPlan", create(type, repaymentPlan, repaymentPlan.getFirstPaymentDate()));
                fullAdmissionResponse.getStatementOfMeans().ifPresent(
                    statementOfMeans -> contentBuilder.putAll(
                        statementOfMeansContentProvider.createContent(statementOfMeans)
                    )
                );
                break;
            default:
                throw new IllegalStateException("Invalid response type " + type);
        }
        return contentBuilder.build();
    }
}
