package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class PaymentPlanMetadata {
    private final PaymentOption paymentOption;
    private final LocalDate paymentDate;
    private final PaymentSchedule frequency;

    static PaymentPlanMetadata fromResponse(Response response) {
        if (response instanceof FullAdmissionResponse) {
            final FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) response;
            return fromPaymentIntention(fullAdmissionResponse.getPaymentIntention());
        }

        if (response instanceof PartAdmissionResponse) {
            final PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
            final Optional<PaymentIntention> optionalPaymentIntention = partAdmissionResponse.getPaymentIntention();
            return optionalPaymentIntention.map(PaymentPlanMetadata::fromPaymentIntention).orElse(null);
        }

        return null;
    }

    static PaymentPlanMetadata fromPaymentIntention(PaymentIntention paymentIntention) {
        final LocalDate paymentDate;
        final PaymentSchedule frequency;

        final Optional<RepaymentPlan> optionalRepaymentPlan = paymentIntention.getRepaymentPlan();
        if (optionalRepaymentPlan.isPresent()) {
            final RepaymentPlan repaymentPlan = optionalRepaymentPlan.get();
            paymentDate = repaymentPlan.getFirstPaymentDate();
            frequency = repaymentPlan.getPaymentSchedule();
        } else {
            paymentDate = paymentIntention.getPaymentDate().orElse(null);
            frequency = null;
        }
        return new PaymentPlanMetadata(
            paymentIntention.getPaymentOption(),
            paymentDate,
            frequency
        );
    }

    static PaymentPlanMetadata fromCountyCourtJudgment(CountyCourtJudgment ccj) {
        final LocalDate paymentDate;
        final PaymentSchedule frequency;

        final Optional<RepaymentPlan> optionalRepaymentPlan = ccj.getRepaymentPlan();
        if (optionalRepaymentPlan.isPresent()) {
            final RepaymentPlan repaymentPlan = optionalRepaymentPlan.get();
            paymentDate = repaymentPlan.getFirstPaymentDate();
            frequency = repaymentPlan.getPaymentSchedule();
        } else {
            paymentDate = ccj.getPayBySetDate().orElse(null);
            frequency = null;
        }
        return new PaymentPlanMetadata(
            ccj.getPaymentOption(),
            paymentDate,
            frequency
        );
    }
}
