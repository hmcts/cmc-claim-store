package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Component
public class RequestForJudgementJsonMapper {

    public JsonObject map(Claim claim) {
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("ccjRequestedOn", DateFormatter.format(claim.getCountyCourtJudgmentRequestedAt()))
            .add("amountWithInterest", claim.getTotalAmountTillToday().orElse(null))
            .add("courtFee", claim.getClaimData().getFeesPaidInPound())
            .add("alreadyPaid", countyCourtJudgment.getPaidAmount().orElse(null))
            .add("payment", getPaymentDeadline(countyCourtJudgment))
            .add("instalmentAmount", countyCourtJudgment.getRepaymentPlan().map(RepaymentPlan::getInstalmentAmount).orElse(null))
            .add("firstPayment", DateFormatter.format(countyCourtJudgment.getRepaymentPlan().map(RepaymentPlan::getFirstPaymentDate).orElse(null)))
            .add("countyCourtJudgement", mapCCJ(claim.getCountyCourtJudgment()))
            .add("fullPaymentDeadline", getPaymentDeadline(countyCourtJudgment))
            .add("claimantEmail", claim.getSubmitterEmail())
            .add("defendantEmail", claim.getDefendantEmail())
            .build();
    }

    public JsonObject mapCCJ(CountyCourtJudgment countyCourtJudgment) {
        Claim claim = Claim.builder().build();
        return new NullAwareJsonObjectBuilder()
            .add("ccjRequestedOn", DateFormatter.format(claim.getCountyCourtJudgmentRequestedAt()))
            .add("paymentOption", countyCourtJudgment.getPaymentOption().getDescription())
            .build();
    }

    private String getPaymentDeadline(CountyCourtJudgment countyCourtJudgment) {
        switch (countyCourtJudgment.getPaymentOption()) {
            case IMMEDIATELY:
                return PaymentOption.IMMEDIATELY.getDescription();
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

