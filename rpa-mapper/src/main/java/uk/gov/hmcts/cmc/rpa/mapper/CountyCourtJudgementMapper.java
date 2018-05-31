package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Component
public class CountyCourtJudgementMapper {

    public JsonObject mapCCJ(CountyCourtJudgment countyCourtJudgment) {
        Claim claim = Claim.builder().build();
        return new NullAwareJsonObjectBuilder()
            .add("ccjRequestedOn", DateFormatter.format(claim.getCountyCourtJudgmentRequestedAt()))
            .add("paymentOption", countyCourtJudgment.getPaymentOption().getDescription())
            .add("firstPayment", DateFormatter.format(countyCourtJudgment.getRepaymentPlan().map(RepaymentPlan::getFirstPaymentDate).orElse(null)))
            .add("fullPaymentDeadline", getPaymentDeadline(countyCourtJudgment))
            .add("payment", countyCourtJudgment.getPaymentOption().getDescription())
            .add("instalmentAmount", countyCourtJudgment.getRepaymentPlan().map(RepaymentPlan::getInstalmentAmount).orElse(null))
            .add("alreadyPaid", countyCourtJudgment.getPaidAmount().orElse(null))
            .build();
    }

    private String getPaymentDeadline(CountyCourtJudgment countyCourtJudgment) {
        switch (countyCourtJudgment.getPaymentOption()) {
            case IMMEDIATELY:
                return "Forthwith";
            case FULL_BY_SPECIFIED_DATE:
                return "In Full by " + DateFormatter.format(countyCourtJudgment.getPayBySetDate().get());
            case INSTALMENTS:
                return getRepaymentSchedule(countyCourtJudgment.getRepaymentPlan().get());
            default:
                throw new IllegalArgumentException("No payment option selected");

        }
    }

    private String getRepaymentSchedule(RepaymentPlan repaymentPlan) {
        PaymentSchedule paymentSchedule = repaymentPlan.getPaymentSchedule();
        String firstPaymentDate = DateFormatter.format(repaymentPlan.getFirstPaymentDate());
        switch (paymentSchedule) {
            case EACH_WEEK:
                return "Weekly + " + firstPaymentDate;
            case EVERY_TWO_WEEKS:
                return "Fortnightly + " + firstPaymentDate;
            case EVERY_MONTH:
                return "Monthly + " + firstPaymentDate;
            default:
                throw new IllegalArgumentException("No Payment Schedule selected");
        }
    }

}
