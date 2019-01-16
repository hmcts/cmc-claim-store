package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention.PaymentIntentionBuilder;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.time.LocalDate;

public class SamplePaymentIntention {

    private SamplePaymentIntention() {
        super();
    }

    public static PaymentIntentionBuilder builder() {
        return PaymentIntention.builder();
    }

    public static PaymentIntention immediately() {
        return builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .paymentDate(LocalDate.now())
            .build();
    }

    public static PaymentIntention bySetDate() {
        return builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().plusDays(10))
            .build();
    }

    public static PaymentIntention instalments() {
        return builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();
    }

    public static PaymentIntention bySetDateInPast() {
        return builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().minusDays(2))
            .build();
    }

}
