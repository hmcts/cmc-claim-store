package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
public class ResponseAcceptationContentProvider {
    public static final String ADMISSIONS_FORM_NO = "OCON9A";

    private final PaymentIntentionContentProvider paymentIntentionContentProvider;

    public ResponseAcceptationContentProvider(
        PaymentIntentionContentProvider paymentIntentionContentProvider
    ) {
        this.paymentIntentionContentProvider = paymentIntentionContentProvider;
    }

    public Map<String, Object> createContent(ResponseAcceptation responseAcceptation) {
        requireNonNull(responseAcceptation);
        Map<String, Object> content = new HashMap<>();

        responseAcceptation.getCourtDetermination().ifPresent(courtDetermination -> {
            PaymentIntention courtDecision = courtDetermination.getCourtDecision();
            content.put("courtDetermination", courtDetermination);
            PaymentIntention courtPaymentIntention = courtDetermination.getCourtPaymentIntention();
            content.putAll(paymentIntentionContentProvider.createContent(
                courtDecision.getPaymentOption(),
                courtDecision.getRepaymentPlan().orElse(null),
                courtDecision.getPaymentDate().orElse(null),
                "The agreed amount",
                "courtDecision"
            ));

            content.putAll(paymentIntentionContentProvider.createContent(
                courtPaymentIntention.getPaymentOption(),
                courtPaymentIntention.getRepaymentPlan().orElse(null),
                courtPaymentIntention.getPaymentDate().orElse(null),
                "The agreed amount",
                "courtPaymentIntention"
                )
            );
        });

        Optional<PaymentIntention> claimantPaymentIntention = responseAcceptation.getClaimantPaymentIntention();
        if (claimantPaymentIntention.isPresent()) {
            content.put("hasClaimantPaymentIntention", true);
            content.put("paymentPlanAccepted", "I reject this repayment plan");
            PaymentIntention paymentIntention = claimantPaymentIntention.get();
            content.putAll(paymentIntentionContentProvider.createContent(
                paymentIntention.getPaymentOption(),
                paymentIntention.getRepaymentPlan().orElse(null),
                paymentIntention.getPaymentDate().orElse(null),
                "The agreed amount",
                ""
            ));
        } else {
            content.put("paymentPlanAccepted", "I accept this repayment plan");
        }

        content.put("formNumber", ADMISSIONS_FORM_NO);

        return content;
    }
}
