package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.utils.ClaimantResponseUtils.isCompanyOrOrganisationWithCCJDetermination;

@Component
public class ResponseAcceptationContentProvider {
    public static final String ADMISSIONS_FORM_NO = "OCON9A";

    static final LocalDate SYSTEM_MAX_DATE = LocalDate.of(9999, 12, 31);
    private final PaymentIntentionContentProvider paymentIntentionContentProvider;

    public ResponseAcceptationContentProvider(
        PaymentIntentionContentProvider paymentIntentionContentProvider
    ) {
        this.paymentIntentionContentProvider = paymentIntentionContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);
        Map<String, Object> content = new HashMap<>();

        ResponseAcceptation responseAcceptation =
            (ResponseAcceptation) claim.getClaimantResponse().orElseThrow(IllegalStateException::new);

        if (!isCompanyOrOrganisationWithCCJDetermination(claim, responseAcceptation)) {
            responseAcceptation.getCourtDetermination().ifPresent(courtDetermination -> {
                PaymentIntention courtDecision = courtDetermination.getCourtDecision();
                content.put("courtDetermination", courtDetermination);
                content.putAll(paymentIntentionContentProvider.createContent(
                    courtDecision.getPaymentOption(),
                    courtDecision.getRepaymentPlan().orElse(null),
                    courtDecision.getPaymentDate().orElse(null),
                    "The agreed amount",
                    "courtDecision"
                ));
                PaymentIntention courtPaymentIntention = courtDetermination.getCourtPaymentIntention();
                Optional<LocalDate> paymentDate = courtPaymentIntention.getPaymentDate();
                if (courtPaymentIntention.getPaymentOption() == PaymentOption.BY_SPECIFIED_DATE
                    && paymentDate.isPresent()
                    && paymentDate.get().equals(SYSTEM_MAX_DATE)) {
                    content.put("hasNegativeDisposableIncome", "The defendant’s disposable income is "
                        + formatMoney(courtDetermination.getDisposableIncome())
                        + ". As such, the court has selected the defendant’s repayment plan.");
                } else {
                    content.putAll(paymentIntentionContentProvider.createContent(
                        courtPaymentIntention.getPaymentOption(),
                        courtPaymentIntention.getRepaymentPlan().orElse(null),
                        paymentDate.orElse(null),
                        "The agreed amount",
                        "courtPaymentIntention"
                        )
                    );
                }
            });
        }

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
