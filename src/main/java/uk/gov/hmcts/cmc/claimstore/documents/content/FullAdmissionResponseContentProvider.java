package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class FullAdmissionResponseContentProvider {

    private final AdmissionContentProvider admissionContentProvider;

    public FullAdmissionResponseContentProvider(
        AdmissionContentProvider admissionContentProvider
    ) {
        this.admissionContentProvider = admissionContentProvider;
    }

    public Map<String, Object> createContent(FullAdmissionResponse fullAdmissionResponse) {
        requireNonNull(fullAdmissionResponse);

        ImmutableMap.Builder<String, Object> contentBuilder = new ImmutableMap.Builder<>();

        contentBuilder.putAll(admissionContentProvider.createPaymentPlanDetails(
            fullAdmissionResponse.getPaymentOption(),
            fullAdmissionResponse.getResponseType(),
            fullAdmissionResponse.getPaymentDate().orElse(null),
            fullAdmissionResponse.getRepaymentPlan().orElse(null))
        );

        fullAdmissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> contentBuilder.putAll(
                admissionContentProvider.createStatementOfMeansContent(statementOfMeans)
            )
        );

        return contentBuilder.build();
    }
}
