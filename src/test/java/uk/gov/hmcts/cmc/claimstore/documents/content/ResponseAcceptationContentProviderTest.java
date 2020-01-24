package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;

public class ResponseAcceptationContentProviderTest {

    private final PaymentIntentionContentProvider paymentIntentionContentProvider =
        new PaymentIntentionContentProvider();

    private final ResponseAcceptation responseAcceptation = ResponseAcceptation.builder()
        .courtDetermination(CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                .build())
            .disposableIncome(BigDecimal.valueOf(-1))
            .decisionType(DecisionType.COURT)
            .build())
        .build();

    private final ResponseAcceptationContentProvider contentProvider =
        new ResponseAcceptationContentProvider(paymentIntentionContentProvider);

    @Test
    public void shouldGetContentSayingDisposableIncomeIsNegativeAndSelectDefendantRepaymentPlan() {
        paymentIntentionContentProvider.createContent(
            PaymentOption.IMMEDIATELY,
            null,
            LocalDate.now(),
            null,
            null
        );

        Map<String, Object> content = contentProvider.createContent(responseAcceptation);

        assertThat(content).containsKeys("hasNegativeDisposableIncome");
        assertThat(content).containsValue("The defendant’s disposable income is -£1."
            + " As such, the court has selected the defendant’s repayment plan.");
    }
}
