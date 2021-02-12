package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.math.BigDecimal;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class FullAdmissionResponseContentProvider {

    public static final String ADMISSIONS_FORM_NO = "OCON9A";

    private final PaymentIntentionContentProvider paymentIntentionContentProvider;
    private final StatementOfMeansContentProvider statementOfMeansContentProvider;

    public FullAdmissionResponseContentProvider(
        PaymentIntentionContentProvider paymentIntentionContentProvider,
        StatementOfMeansContentProvider statementOfMeansContentProvider
    ) {
        this.paymentIntentionContentProvider = paymentIntentionContentProvider;
        this.statementOfMeansContentProvider = statementOfMeansContentProvider;
    }

    public Map<String, Object> createContent(FullAdmissionResponse fullAdmissionResponse,
                                             BigDecimal claimAmountTillDate) {
        requireNonNull(fullAdmissionResponse);
        ImmutableMap.Builder<String, Object> contentBuilder = new ImmutableMap.Builder<String, Object>()
            .put("responseTypeSelected", fullAdmissionResponse.getResponseType().getDescription());

        if (fullAdmissionResponse.getPaymentIntention() != null
            && fullAdmissionResponse.getPaymentIntention().getPaymentOption() != null) {
            contentBuilder.putAll(paymentIntentionContentProvider.createContent(
                fullAdmissionResponse.getPaymentIntention().getPaymentOption(),
                fullAdmissionResponse.getPaymentIntention().getRepaymentPlan().orElse(null),
                fullAdmissionResponse.getPaymentIntention().getPaymentDate().orElse(null),
                formatMoney(claimAmountTillDate), ""
                )
            );
        }

        fullAdmissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> contentBuilder.putAll(
                statementOfMeansContentProvider.createContent(statementOfMeans)
            )
        );
        contentBuilder.put("formNumber", ADMISSIONS_FORM_NO);
        return contentBuilder.build();
    }
}
