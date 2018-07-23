package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class FullAdmissionResponseContentProvider {

    private final PaymentIntentionContentProvider paymentIntentionContentProvider;
    private final StatementOfMeansContentProvider statementOfMeansContentProvider;

    public FullAdmissionResponseContentProvider(
        PaymentIntentionContentProvider paymentIntentionContentProvider,
        StatementOfMeansContentProvider statementOfMeansContentProvider
    ) {
        this.paymentIntentionContentProvider = paymentIntentionContentProvider;
        this.statementOfMeansContentProvider = statementOfMeansContentProvider;
    }

    public Map<String, Object> createContent(FullAdmissionResponse fullAdmissionResponse) {
        requireNonNull(fullAdmissionResponse);

        ImmutableMap.Builder<String, Object> contentBuilder = new ImmutableMap.Builder<String, Object>()
            .put("responseTypeSelected", fullAdmissionResponse.getResponseType().getDescription())
            .putAll(paymentIntentionContentProvider.createContent(
                fullAdmissionResponse.getPaymentIntention().getPaymentOption(),
                fullAdmissionResponse.getPaymentIntention().getRepaymentPlan().orElse(null),
                fullAdmissionResponse.getPaymentIntention().getPaymentDate().orElse(null),
                "The full amount"
                )
            );

        fullAdmissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> contentBuilder.putAll(
                statementOfMeansContentProvider.createContent(statementOfMeans)
            )
        );

        return contentBuilder.build();
    }
}
