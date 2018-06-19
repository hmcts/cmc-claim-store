package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.FULL_BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;

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
        Optional<RepaymentPlan> repaymentPlan = fullAdmissionResponse.getRepaymentPlan();
        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();

        if (type.equals(IMMEDIATELY)) {
            contentBuilder.put("paymentOption", type.getDescription());
        }

        if (type.equals(FULL_BY_SPECIFIED_DATE)) {
            contentBuilder.put("paymentOption", type.getDescription());
            fullAdmissionResponse.getStatementOfMeans().ifPresent(
                statementOfMeans -> contentBuilder.putAll(statementOfMeansContentProvider.createContent(statementOfMeans))
            );
        }

        if (type.equals(INSTALMENTS)) {
            contentBuilder.put("paymentOption", type.getDescription());
            contentBuilder.put("repaymentPlan", repaymentPlan);
            fullAdmissionResponse.getStatementOfMeans().ifPresent(
                statementOfMeans -> contentBuilder.putAll(statementOfMeansContentProvider.createContent(statementOfMeans))
            );
        }

        return contentBuilder.build();
    }
}
